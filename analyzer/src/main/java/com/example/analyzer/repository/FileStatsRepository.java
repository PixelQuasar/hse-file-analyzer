package com.example.analyzer.repository;

import com.example.analyzer.entity.FileStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileStatsRepository extends JpaRepository<FileStats, Long> {

    Optional<FileStats> findByFileId(String fileId);

    boolean existsByFileId(String fileId);
}
