package com.example.analyzer.consumer;

import com.example.analyzer.dto.event.FileUploadedEvent;
import com.example.analyzer.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileUploadConsumer {

    private final AnalysisService analysisService; // Сервис, который будет выполнять реальную работу

    // Указываем containerFactory, который мы определили в KafkaConsumerConfig
    @KafkaListener(
            topics = "${kafka.topic.files.uploaded}",
            groupId = "${spring.kafka.consumer.group-id}", // groupId также можно указать здесь, если он одинаков для всех listener'ов
            containerFactory = "fileUploadedKafkaListenerContainerFactory"
    )
    public void consumeFileUploadedEvent(@Payload FileUploadedEvent event,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                         @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received FileUploadedEvent: fileId='{}', originalFilename='{}', storagePath='{}', userId='{}' from topic='{}', partition='{}', offset='{}'",
                event.getFileId(), event.getOriginalFilename(), event.getStoragePath(), event.getUserId(),
                topic, partition, offset);

        try {
            analysisService.processFile(event);
        } catch (Exception e) {
            // Здесь важна стратегия обработки ошибок.
            // 1. Логирование - обязательно.
            // 2. Повторы (retries) - можно настроить на уровне KafkaListenerContainerFactory.
            // 3. Dead Letter Queue (DLQ) - для сообщений, которые не удалось обработать после нескольких попыток.
            //    Это предотвращает блокировку консьюмера "ядовитыми" сообщениями.
            //    DLQ - это отдельный топик, куда отправляются такие сообщения.
            //    Spring Kafka предоставляет DeadLetterPublishingRecoverer для этого.
            log.error("Error processing FileUploadedEvent for fileId {}: {}", event.getFileId(), e.getMessage(), e);
            // Если не настроен специальный ErrorHandler, который перехватывает исключения,
            // то исключение здесь приведет к тому, что offset не будет закоммичен,
            // и сообщение будет пытаться обработаться снова (в зависимости от настроек retries).
            // Для простоты, сейчас мы просто логируем. Для продакшена нужна более robust обработка.
        }
    }
}
