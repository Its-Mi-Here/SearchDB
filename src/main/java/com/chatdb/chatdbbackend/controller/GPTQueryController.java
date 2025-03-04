package com.chatdb.chatdbbackend.controller;

import com.chatdb.chatdbbackend.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gpt-query")
@CrossOrigin(origins = "*")
public class GPTQueryController {

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/generate-and-execute")
    public Map<String, Object> generateAndExecuteQuery(@RequestBody Map<String, String> request) {
        String userQuery = request.get("query");
        String dbType = request.get("database");

        try {
            // Generate the query using GPT
            String generatedQuery = openAIService.generateQuery(userQuery, dbType);

            if (dbType.equalsIgnoreCase("mongo")) {
                return Map.of("generatedQuery", generatedQuery, "message", "MongoDB Query Generated");
            } else {
                // Execute SQL Query for PostgreSQL
                List<Map<String, Object>> results = jdbcTemplate.queryForList(generatedQuery);
                return Map.of("generatedQuery", generatedQuery, "results", results);
            }
        } catch (IOException e) {
            return Map.of("error", "Failed to connect to OpenAI API.");
        } catch (Exception e) {
            return Map.of("error", "SQL Execution Failed: " + e.getMessage());
        }
    }
}
