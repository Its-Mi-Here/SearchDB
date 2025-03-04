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

    public String generateSQLQuery(String userQuery) throws IOException {
        logger.info("🔍 Received user query: " + userQuery);

        String jsonRequest = objectMapper.writeValueAsString(Map.of(
                "model", "gpt-4",
                "messages", new Object[]{
                        Map.of("role", "system", "content", "You are an AI that generates SQL queries from natural language."),
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

            // Extract SQL query from OpenAI's JSON response
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            String generatedSQL = ((Map<String, String>) ((Map<String, Object>) ((java.util.List<?>) result.get("choices")).get(0)).get("message")).get("content");

            logger.info("✅ Extracted SQL Query: " + generatedSQL);
            return generatedSQL;

        } catch (Exception e) {
            logger.severe("⚠️ Error while processing OpenAI response: " + e.getMessage());
            throw e;
        }
    }
}
