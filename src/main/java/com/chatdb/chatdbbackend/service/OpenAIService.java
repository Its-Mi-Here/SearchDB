package com.chatdb.chatdbbackend.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import okhttp3.*;
import org.springframework.jdbc.core.JdbcTemplate;


@Service
public class OpenAIService {

    private static final Logger logger = Logger.getLogger(OpenAIService.class.getName());

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MongoDBService mongoDBService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> generateAndExecute(Map<String, String> request) {
        String userQuery = request.get("query");
        String dbType = request.get("database");

        try {
            String generatedQuery = this.generateQuery(userQuery, dbType);

            if (dbType.equalsIgnoreCase("mongo")) {
                Object mongoResultObject = mongoDBService.executeMongoCommand(generatedQuery);
                return Map.of("generatedQuery", generatedQuery, "results", mongoResultObject);
            }
            else {
                // Execute SQL Query for PostgreSQL
                List<Map<String, Object>> results = jdbcTemplate.queryForList(generatedQuery);
                return Map.of("generatedQuery", generatedQuery, "results", results);
            }
        }
        catch (IOException e) {
            System.out.println("IO exception: " + e);
            return Map.of("error", "Failed to connect to OpenAI API.");
        }
        catch (Exception e) {
            System.out.println("Exception in generateAndExecute: " + e);
            return Map.of("error", "SQL Execution Failed: " + e.getMessage());
        }
    }

    public String generateQuery(String userQuery, String dbType) throws IOException {
        logger.info("🔍 Received user query: " + userQuery + " for database: " + dbType);

        // Define system prompt based on database type
        String systemPrompt = dbType.equalsIgnoreCase("mongo") ?
                "You are an AI that converts natural language into MongoDB shell queries. Generate only the valid MongoDB query." :
                "You are an AI that converts natural language into SQL queries. Generate only the valid SQL query in one line.";

        String jsonRequest = objectMapper.writeValueAsString(Map.of(
                "model", "gpt-4",
                "messages", new Object[]{
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userQuery)
                }
        ));

        logger.info("📤 Sending request to OpenAI: " + jsonRequest);

        RequestBody body = RequestBody.create(jsonRequest, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.severe("❌ OpenAI API Error: " + response.code() + " - " + response.message());
                throw new IOException("Unexpected OpenAI API response: " + response);
            }

            String responseBody = response.body().string();
            logger.info("📥 OpenAI Response: " + responseBody);

            // Extract query from OpenAI response
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            return ((Map<String, String>) ((Map<String, Object>) ((java.util.List<?>) result.get("choices")).get(0)).get("message")).get("content");
        }
    }
}
