package com.chatdb.chatdbbackend.controller;

import com.chatdb.chatdbbackend.service.KafkaProducerService;
import com.chatdb.chatdbbackend.service.OpenAIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gpt-query")
@CrossOrigin(origins = "*")

public class QueryController {

    private final OpenAIService openAIService;
    private final KafkaProducerService producerService;
    public QueryController(OpenAIService openAIService, KafkaProducerService producerService) {
        this.openAIService = openAIService;
        this.producerService = producerService;
    }


    @GetMapping("/")
    public ResponseEntity<Map<String, Object>>  initialDisplay() {
        Map<String, Object> response = openAIService.startUpDisplay();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonOutput = objectMapper.writeValueAsString(response);
            System.out.println("Backend JSON Response: " + jsonOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(response);

    };

    @PostMapping("/add-entry-to-db")
    public ResponseEntity<Map<String, Object>> addEntryToDB(@RequestBody Map<String, Object> entry) {
        Map<String, Object> response = openAIService.addEntryToDB(entry);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonOutput = objectMapper.writeValueAsString(response);
            System.out.println("Backend JSON Response: " + jsonOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        producerService.publishEntryToDB("Entry added to the database: notification via new topic- db-entry!" );
//        producerService.publishMessage("Entry added to db - via topic my-topic");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-and-execute-query")
    public ResponseEntity<Map<String, Object>> generateAndExecuteQuery(@RequestBody Map<String, String> request){
        Map<String, Object> response = openAIService.generateAndExecute(request);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonOutput = objectMapper.writeValueAsString(response);
            System.out.println("Backend JSON Response: " + jsonOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(response);
    }
}