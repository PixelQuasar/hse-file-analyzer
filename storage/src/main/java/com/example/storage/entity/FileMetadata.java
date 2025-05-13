package com.example.storage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp; // или jakarta.persistence.PrePersist для установки вручную

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@NoArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fileId;

    @Column(nullable = false)
    private String originalFilename;

    @Column
    private String contentType;

    @Column
    private long size;

    @Column(nullable = false)
    private String storagePath;

    @Column
    private String userId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadTimestamp;

    public FileMetadata(String fileId, String originalFilename, String contentType, long size, String storagePath, String userId) {
        this.fileId = fileId;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.storagePath = storagePath;
        this.userId = userId;
    }
}
