package com.example.analyzer.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static class ErrorDetails {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;

        public ErrorDetails(LocalDateTime timestamp, int status, String error, String message, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }
        // Геттеры для Jackson
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
    }


    @ExceptionHandler(AnalysisException.class)
    public ResponseEntity<Object> handleAnalysisException(
            AnalysisException ex, WebRequest request) {

        ErrorDetails body = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // Можно выбрать более подходящий статус
                "Analysis Error",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Обработчик для ошибок, связанных с вызовом внешних сервисов (например, FileStorageClient)
    @ExceptionHandler(ExternalServiceException.class) // Предположим, у вас есть такое исключение
    public ResponseEntity<Object> handleExternalServiceException(
            ExternalServiceException ex, WebRequest request) {
        logger.warn("External service call failed: {}", ex.getMessage());
        ErrorDetails body = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.BAD_GATEWAY.value(),
                "External Service Error",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_GATEWAY);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(
            Exception ex, WebRequest request) {
        logger.error("An unexpected error occurred in Analyzer service: ", ex);

        ErrorDetails body = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred during analysis.",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
