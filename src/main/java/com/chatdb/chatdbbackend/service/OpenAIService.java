package com.chatdb.chatdbbackend.service;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class OpenAIService {

    private static final Logger logger = Logger.getLogger(OpenAIService.class.getName());

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateQuery(String userQuery, String dbType) throws IOException {
        logger.info("🔍 Received user query: " + userQuery + " for database: " + dbType);

        // Define system prompt based on database type
        String systemPrompt = dbType.equalsIgnoreCase("mongo") ?
                "You are an AI that converts natural language into MongoDB shell queries. Generate only the valid MongoDB query." :
                "You are an AI that converts natural language into SQL queries. Generate only the valid SQL query.";

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
