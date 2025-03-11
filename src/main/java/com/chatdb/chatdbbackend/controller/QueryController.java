package com.chatdb.chatdbbackend.controller;

import com.chatdb.chatdbbackend.service.OpenAIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gpt-query")
@CrossOrigin(origins = "*")

public class QueryController {

    private OpenAIService openAIService;
    public QueryController(OpenAIService openAIService) {
        this.openAIService = openAIService;
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