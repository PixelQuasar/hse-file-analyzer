package com.example.analyzer.service;

import com.example.analyzer.dto.event.PlagiarismCheckResultEvent;
import com.example.analyzer.entity.FileHash;
import com.example.analyzer.repository.FileHashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlagiarismDetectionService {

    private final FileHashRepository fileHashRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.plagiarism.checked}")
    private String plagiarismCheckedTopic;

    private static final String HASH_ALGORITHM = "SHA-256";

    @Transactional
    public void detectAndReportPlagiarism(String fileId, String fileContent) {
        log.info("Starting plagiarism detection for fileId: {}", fileId);

        String currentFileHash;
        try {
            currentFileHash = calculateHash(fileContent);
        } catch (NoSuchAlgorithmException e) {
            log.error("Algorithm {} not found for hashing fileId: {}", HASH_ALGORITHM, fileId, e);
            return;
        }

        if (fileHashRepository.existsByFileId(fileId)) {
            log.warn("Plagiarism check for fileId {} already performed. Skipping.", fileId);
            return;
        }


        List<FileHash> duplicateHashes = fileHashRepository.findByHashAlgorithmAndHashValue(HASH_ALGORITHM, currentFileHash);

        boolean isPlagiarized = false;
        String matchedFileId = null;

        if (!duplicateHashes.isEmpty()) {
            FileHash firstDuplicate = duplicateHashes.get(0);
            if (!firstDuplicate.getFileId().equals(fileId)) {
                isPlagiarized = true;
                matchedFileId = firstDuplicate.getFileId();
                log.info("Plagiarism detected for fileId: {}. Matches with fileId: {}", fileId, matchedFileId);
            }
        }

        FileHash newFileHash = new FileHash(fileId, HASH_ALGORITHM, currentFileHash);
        fileHashRepository.save(newFileHash);
        log.debug("Saved hash for fileId: {}", fileId);

        PlagiarismCheckResultEvent event = new PlagiarismCheckResultEvent(
                fileId,
                isPlagiarized,
                matchedFileId,
                isPlagiarized ? 100.0 : 0.0
        );
        kafkaTemplate.send(plagiarismCheckedTopic, fileId, event);
        log.info("Sent PlagiarismCheckResultEvent to Kafka for fileId: {}, isPlagiarized: {}", fileId, isPlagiarized);
    }

    private String calculateHash(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] encodedhash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
