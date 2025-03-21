package com.chatdb.chatdbbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cacheable(value = "openAiQueries", key = "#userQuery + '-' + #dbType")
    public String generateQuery(String userQuery, String dbType) throws IOException {
        System.out.println("Received user query: " + userQuery + " for database: " + dbType);

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

        System.out.println("Sending request to OpenAI: " + jsonRequest);

        RequestBody body = RequestBody.create(jsonRequest, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("OpenAI API Error: " + response.code() + " - " + response.message());
                throw new IOException("Unexpected OpenAI API response: " + response);
            }

            String responseBody = response.body().string();
            System.out.println("OpenAI Response: " + responseBody);

            // Extract query from OpenAI response
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            return ((Map<String, String>) ((Map<String, Object>) ((List<?>) result.get("choices")).get(0)).get("message")).get("content");
        }
    }

}
