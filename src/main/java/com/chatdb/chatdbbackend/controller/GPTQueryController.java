package com.chatdb.chatdbbackend.controller;

import com.chatdb.chatdbbackend.service.OpenAIService;
import com.chatdb.chatdbbackend.model.QueryRequest;
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
    public Map<String, Object> generateAndExecuteQuery(@RequestBody QueryRequest request) {
        String userQuery = request.getQuery();

        try {
            // Generate SQL Query from GPT API
            String sqlQuery = openAIService.generateSQLQuery(userQuery);

            // Execute the generated SQL Query
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);

            return Map.of("generatedQuery", sqlQuery, "results", results);
        } catch (IOException e) {
            return Map.of("error", "Failed to connect to OpenAI API.");
        } catch (Exception e) {
            return Map.of("error", "SQL Execution Failed: " + e.getMessage());
        }
    }
}
