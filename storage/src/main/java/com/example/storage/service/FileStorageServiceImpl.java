package com.example.storage.service;

import com.example.storage.config.StorageProperties;
import com.example.storage.dto.FileUploadResponseDTO;
import com.example.storage.dto.event.FileUploadedEvent;
import com.example.storage.entity.FileMetadata;
import com.example.storage.exception.FileNotFoundException;
import com.example.storage.exception.FileStorageException;
import com.example.storage.repository.FileMetadataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl {

    private final FileMetadataRepository fileMetadataRepository;
    private final KafkaTemplate<String, FileUploadedEvent> kafkaTemplate;
    private final StorageProperties storageProperties;

    @Value("${kafka.topic.files.uploaded}")
    private String filesUploadedTopic;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Created storage directory: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Transactional
    public FileUploadResponseDTO storeFile(MultipartFile file, String userId) {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileId = UUID.randomUUID().toString();

        String storedFilename = fileId + "_" + originalFilename;

        try {
            if (originalFilename.contains("..")) {
                log.warn("Filename contains invalid path sequence: {}", originalFilename);
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFilename);
            }

            Path targetLocation = this.fileStorageLocation.resolve(storedFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                log.info("Stored file {} to {}", originalFilename, targetLocation);
            }

            FileMetadata metadata = new FileMetadata(
                    fileId,
                    originalFilename,
                    file.getContentType(),
                    file.getSize(),
                    targetLocation.toString(),
                    userId
            );
            fileMetadataRepository.save(metadata);
            log.info("Saved metadata for fileId: {}", fileId);

            FileUploadedEvent event = new FileUploadedEvent(
                    fileId,
                    originalFilename,
                    file.getContentType(),
                    file.getSize(),
                    targetLocation.toString(),
                    userId
            );
            kafkaTemplate.send(filesUploadedTopic, fileId, event); // fileId как ключ сообщения
            log.info("Sent FileUploadedEvent to Kafka topic {} for fileId: {}", filesUploadedTopic, fileId);

            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/files/")
                    .path(fileId)
                    .toUriString();

            return new FileUploadResponseDTO(
                    fileId,
                    originalFilename,
                    file.getContentType(),
                    file.getSize(),
                    "File uploaded successfully",
                    fileDownloadUri
            );

        } catch (IOException ex) {
            log.error("Could not store file {}. Please try again!", originalFilename, ex);
            throw new FileStorageException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(String fileId) {
        try {
            FileMetadata metadata = fileMetadataRepository.findByFileId(fileId)
                    .orElseThrow(() -> {
                        log.warn("File not found with fileId: {}", fileId);
                        return new FileNotFoundException("File not found with id " + fileId);
                    });

            Path filePath = Paths.get(metadata.getStoragePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                log.error("Could not read file from path: {}", filePath);
                throw new FileStorageException("Could not read file: " + metadata.getOriginalFilename());
            }
        } catch (MalformedURLException ex) {
            log.error("Malformed URL for fileId: {}", fileId, ex);
            throw new FileNotFoundException("File not found with id (malformed URL) " + fileId, ex);
        }
    }

    @Transactional(readOnly = true)
    public FileMetadata getFileMetadata(String fileId) {
        return fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> {
                    log.warn("Metadata not found for fileId: {}", fileId);
                    return new FileNotFoundException("Metadata not found for file with id " + fileId);
                });
    }
}
