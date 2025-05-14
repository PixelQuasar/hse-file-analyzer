package com.example.analyzer.client;

import org.springframework.core.io.Resource;

public interface FileStorageClient {

    Resource downloadFileAsResource(String fileId);

}

