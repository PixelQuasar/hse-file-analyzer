package com.example.analyzer.service;

import com.example.analyzer.client.FileStorageClient;
import com.example.analyzer.dto.event.FileStatsCalculatedEvent;
import com.example.analyzer.dto.event.FileUploadedEvent;
import com.example.analyzer.entity.FileStats;
import com.example.analyzer.exception.AnalysisException;
import com.example.analyzer.repository.FileStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final FileStorageClient fileStorageClient;
    private final FileStatsRepository fileStatsRepository;
    private final PlagiarismDetectionService plagiarismDetectionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.stats.calculated}")
    private String statsCalculatedTopic;

    @Transactional
    public void processFile(FileUploadedEvent event) {
        String fileId = event.getFileId();
        log.info("Processing fileId: {}", fileId);

        if (fileStatsRepository.existsByFileId(fileId)) {
            log.warn("Statistics for fileId {} already calculated. Skipping statistics calculation.", fileId);
            try {
                Resource resource = fileStorageClient.downloadFileAsResource(fileId);
                String fileContent = resourceToString(resource);
                plagiarismDetectionService.detectAndReportPlagiarism(fileId, fileContent);
            } catch (IOException e) {
                log.error("IOException while reprocessing file {} for plagiarism check: {}", fileId, e.getMessage(), e);
                throw new AnalysisException("Failed to reprocess file for plagiarism check " + fileId, e);
            }
            return;
        }

        try {
            Resource resource = fileStorageClient.downloadFileAsResource(fileId);
            String fileContent = resourceToString(resource);
            log.debug("Successfully downloaded and read file content for fileId: {}", fileId);

            FileStats stats = calculateStatistics(fileId, fileContent);
            fileStatsRepository.save(stats);
            log.info("Saved statistics for fileId: {}", fileId);

            FileStatsCalculatedEvent statsEvent = new FileStatsCalculatedEvent(
                    fileId,
                    stats.getParagraphCount(),
                    stats.getWordCount(),
                    stats.getCharCount()
            );
            kafkaTemplate.send(statsCalculatedTopic, fileId, statsEvent);
            log.info("Sent FileStatsCalculatedEvent to Kafka for fileId: {}", fileId);

            plagiarismDetectionService.detectAndReportPlagiarism(fileId, fileContent);

            log.info("Successfully processed fileId: {}", fileId);

        } catch (IOException e) {
            log.error("IOException while processing file {}: {}", fileId, e.getMessage(), e);
            throw new AnalysisException("Failed to process file " + fileId, e);
        } catch (Exception e) {
            log.error("Unexpected error while processing file {}: {}", fileId, e.getMessage(), e);
            throw new AnalysisException("Unexpected error during file processing for " + fileId, e);
        }
    }

    private FileStats calculateStatistics(String fileId, String content) {
        int charCount = content.length();
        int wordCount = content.isEmpty() ? 0 : content.trim().split("\\s+").length;
        int paragraphCount = content.isEmpty() ? 0 : content.split("(\r\n|\r|\n){2,}|(^\n|\n$)|(^\r\n|\r\n$)").length;
        if (!content.trim().isEmpty() && paragraphCount == 0) {
            paragraphCount = 1;
        }
        if (content.trim().isEmpty()) {
            paragraphCount = 0;
            wordCount = 0;
        }


        log.debug("Calculated stats for fileId {}: Chars={}, Words={}, Paragraphs={}", fileId, charCount, wordCount, paragraphCount);
        return new FileStats(fileId, paragraphCount, wordCount, charCount);
    }

    private String resourceToString(Resource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
