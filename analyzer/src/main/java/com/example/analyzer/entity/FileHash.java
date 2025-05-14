package com.example.analyzer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_hashes", indexes = {
        @Index(name = "idx_filehash_hashvalue", columnList = "hashValue")
})
@Getter
@Setter
@NoArgsConstructor
public class FileHash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fileId;

    @Column(nullable = false, length = 64)
    private String hashAlgorithm;

    @Column(nullable = false, length = 255)
    private String hashValue;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FileHash(String fileId, String hashAlgorithm, String hashValue) {
        this.fileId = fileId;
        this.hashAlgorithm = hashAlgorithm;
        this.hashValue = hashValue;
    }
}
