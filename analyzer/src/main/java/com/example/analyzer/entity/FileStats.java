package com.example.analyzer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_stats")
@Getter
@Setter
@NoArgsConstructor
public class FileStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fileId;

    private int paragraphCount;
    private int wordCount;
    private int charCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public FileStats(String fileId, int paragraphCount, int wordCount, int charCount) {
        this.fileId = fileId;
        this.paragraphCount = paragraphCount;
        this.wordCount = wordCount;
        this.charCount = charCount;
    }
}
