package com.example.storage.controller;

import com.example.storage.dto.FileUploadResponseDTO;
import com.example.storage.entity.FileMetadata;
import com.example.storage.service.FileStorageServiceImpl;
import jakarta.servlet.http.HttpServletRequest; // Важно: jakarta.servlet для Spring Boot 3
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageServiceImpl fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file,
                                                            @RequestParam(value = "userId", required = false) String userId) {
        if (file.isEmpty()) {
            log.warn("Upload attempt with an empty file.");
        }
        log.info("Received file upload request for original filename: {}, userId: {}", file.getOriginalFilename(), userId);
        FileUploadResponseDTO response = fileStorageService.storeFile(file, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId, HttpServletRequest request) {
        log.info("Received file download request for fileId: {}", fileId);
        Resource resource = fileStorageService.loadFileAsResource(fileId);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.warn("Could not determine file type for resource: {}", resource.getFilename(), ex);
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        FileMetadata metadata = fileStorageService.getFileMetadata(fileId);


        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalFilename() + "\"") // metadata.getOriginalFilename() корректнее, чем resource.getFilename() который может содержать fileId
                .body(resource);
    }

    @GetMapping("/{fileId}/metadata")
    public ResponseEntity<FileMetadata> getFileMetadata(@PathVariable String fileId) {
        log.info("Received metadata request for fileId: {}", fileId);
        FileMetadata metadata = fileStorageService.getFileMetadata(fileId);
        return ResponseEntity.ok(metadata);
    }
}
