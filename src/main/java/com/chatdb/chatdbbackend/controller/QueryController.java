package com.chatdb.chatdbbackend.controller;

import com.chatdb.chatdbbackend.service.OpenAIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gpt-query")
@CrossOrigin(origins = "*")

public class QueryController {

    private OpenAIService openAIService;
    public QueryController(OpenAIService openAIService) {
        this.openAIService = openAIService;
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