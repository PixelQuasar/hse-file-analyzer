package com.example.analyzer.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileStatsCalculatedEvent {
    private String fileId;
    private int paragraphCount;
    private int wordCount;
    private int charCount;
}
