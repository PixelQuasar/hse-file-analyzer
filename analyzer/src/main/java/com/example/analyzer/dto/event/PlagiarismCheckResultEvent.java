package com.example.analyzer.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlagiarismCheckResultEvent {
    private String fileId;
    private boolean plagiarized;
    private String matchedFileId;
    private double similarityPercentage;
}
