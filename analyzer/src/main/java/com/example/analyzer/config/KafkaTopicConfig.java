package com.example.analyzer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.stats.calculated}")
    private String statsCalculatedTopicName;

    @Value("${kafka.topic.plagiarism.checked}")
    private String plagiarismCheckedTopicName;

    @Value("${kafka.default-topic.partitions:1}")
    private int defaultPartitions;

    @Value("${kafka.default-topic.replicas:1}")
    private int defaultReplicas;

    @Bean
    public NewTopic statsCalculatedTopic() {
        return TopicBuilder.name(statsCalculatedTopicName)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }

    @Bean
    public NewTopic plagiarismCheckedTopic() {
        return TopicBuilder.name(plagiarismCheckedTopicName)
                .partitions(defaultPartitions)
                .replicas(defaultReplicas)
                .build();
    }
}
