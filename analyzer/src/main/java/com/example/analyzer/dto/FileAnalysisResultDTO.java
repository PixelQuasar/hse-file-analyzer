package com.example.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileAnalysisResultDTO {
    private String fileId;
    private Integer paragraphCount;
    private Integer wordCount;
    private Integer charCount;
    private Boolean isPlagiarized;
    private String matchedFileId;
}
