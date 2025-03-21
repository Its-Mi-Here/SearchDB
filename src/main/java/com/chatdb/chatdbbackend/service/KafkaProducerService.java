package com.chatdb.chatdbbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishMessage(String message) {
        kafkaTemplate.send("my-topic", message);
    }

    public void publishEntryToDB(String message) {
        System.out.println("Publishing message through db-entry: "+ message);
        kafkaTemplate.send("db-entry", message);
    }
}
