package com.example.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponseDTO {
    private String fileId;
    private String fileName;
    private String fileType;
    private long size;
    private String message;
    private String downloadUri;
}
