package com.example.analyzer.client;

import com.example.analyzer.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileStorageClientImpl implements FileStorageClient {

    private final WebClient fileStorageWebClient;

    @Override
    public Resource downloadFileAsResource(String fileId) {
        log.debug("Attempting to download file with id: {}", fileId);
        try {
            return fileStorageWebClient.get()
                    .uri("/files/{fileId}", fileId)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        String errorMsg = String.format(
                                                "Error downloading file %s from storage service. Status: %s, Body: %s",
                                                fileId, clientResponse.statusCode(), errorBody
                                        );
                                        log.warn(errorMsg);
                                        return Mono.error(new ExternalServiceException(errorMsg));
                                    })
                    )
                    .bodyToMono(Resource.class)
                    .block();
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Unexpected error while trying to download file %s from storage service: %s", fileId, e.getMessage());
            log.error(errorMsg, e);
            throw new ExternalServiceException(errorMsg, e);
        }
    }
}
