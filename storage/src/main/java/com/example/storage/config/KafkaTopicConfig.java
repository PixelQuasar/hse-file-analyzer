package com.example.storage.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.files.uploaded}")
    private String filesUploadedTopicName;

    @Value("${kafka.topic.files.uploaded.partitions:1}")
    private int filesUploadedTopicPartitions;

    @Value("${kafka.topic.files.uploaded.replicas:1}")
    private int filesUploadedTopicReplicas;

    @Bean
    public NewTopic filesUploadedTopic() {
        return TopicBuilder.name(filesUploadedTopicName)
                .partitions(filesUploadedTopicPartitions)
                .replicas(filesUploadedTopicReplicas)
                .build();
    }
}
