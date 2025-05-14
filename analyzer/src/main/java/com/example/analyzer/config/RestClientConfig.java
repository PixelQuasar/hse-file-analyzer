package com.example.analyzer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RestClientConfig {

    @Value("${app.services.file-storage.base-url}")
    private String fileStorageBaseUrl;

    @Bean
    public WebClient fileStorageWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(fileStorageBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // Можно добавить другие default-настройки: таймауты, фильтры и т.д.
                .build();
    }
}
