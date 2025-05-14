package com.example.analyzer.repository;

import com.example.analyzer.entity.FileHash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileHashRepository extends JpaRepository<FileHash, Long> {

    Optional<FileHash> findByFileId(String fileId);

    List<FileHash> findByHashAlgorithmAndHashValue(String hashAlgorithm, String hashValue);

    boolean existsByFileId(String fileId);
}
