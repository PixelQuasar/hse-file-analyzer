package com.example.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    @Value("${app.services.file-storage.url}")
    private String fileStorageServiceUrl;

     @Value("${app.services.file-analyzer.url}")
     private String fileAnalyzerServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // --- Маршруты для File Storage Service ---
                .route("file_storage_upload", r -> r.path("/api/v1/gateway/storage/upload") // Путь, который слушает Gateway
                        .and().method(HttpMethod.POST)
                        .filters(f -> f.rewritePath("/api/v1/gateway/storage/(?<segment>.*)", "/api/v1/files/${segment}")
                                // Можно добавить другие фильтры, например, для изменения размера запроса, если нужно
                                // f.requestSize(10 * 1024 * 1024) // Пример: 10MB
                        )
                        .uri(fileStorageServiceUrl)) // URI сервиса, куда перенаправлять

                .route("file_storage_download", r -> r.path("/api/v1/gateway/storage/{fileId}")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.rewritePath("/api/v1/gateway/storage/(?<segment>.*)", "/api/v1/files/${segment}"))
                        .uri(fileStorageServiceUrl))

                .route("file_storage_metadata", r -> r.path("/api/v1/gateway/storage/{fileId}/metadata")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.rewritePath("/api/v1/gateway/storage/(?<segment>.*)", "/api/v1/files/${segment}"))
                        .uri(fileStorageServiceUrl))

                .route("file_analyzer_results", r -> r.path("/api/v1/gateway/analyzer/results/{fileId}")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.rewritePath("/api/v1/gateway/analyzer/(?<segment>.*)", "/api/v1/analysis/${segment}")) // Предполагаемый путь на анализаторе
                        .uri(fileAnalyzerServiceUrl))
                .build();
    }
}
