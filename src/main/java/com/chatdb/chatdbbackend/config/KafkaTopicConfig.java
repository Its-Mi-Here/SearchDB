package com.chatdb.chatdbbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic myTopic() {
        // Creates a new topic named "my-topic" with 1 partition and 1 replica
        return TopicBuilder
                .name("my-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic databaseEntry() {
        return TopicBuilder
                .name("db-entry")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
