package com.example.analyzer.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadedEvent {
    private String fileId;
    private String originalFilename;
    private String contentType;
    private long size;
    private String storagePath;
    private String userId;
}
