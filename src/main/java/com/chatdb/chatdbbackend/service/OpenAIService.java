package com.chatdb.chatdbbackend.service;
import com.chatdb.chatdbbackend.model.Employees;
import com.chatdb.chatdbbackend.repo.EmployeeRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    @Autowired
    EmployeeRepo repo;

    public Map<String, Object> startUpDisplay() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Employees> employeesList = repo.findAll(); // Fetch all employees
            response.put("status", "success");
            response.put("results", employeesList);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to fetch employees: " + e.getMessage());
        }

        return response;
    }

    public Map<String, Object> addEntryToDB(Map<String, Object> entry) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("Entry: " + entry);

        try{
            Employees employee = new Employees();
//            employee.setEmployee_id((Integer) entry.get("id") );
            employee.setFirst_name((String) entry.get("first_name") );
            employee.setLast_name((String) entry.get("last_name") );
            employee.setEmail((String) entry.get("email") );
            employee.setJob_title( (String) entry.get("job_title") );

            String hireDateString = (String) entry.get("hire_date");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date hireDate = dateFormat.parse(hireDateString);
            employee.setHire_date(hireDate);
            repo.save(employee);

            List<Employees> employeesList = repo.findAll(); // Fetch all employees
            response.put("status", "success");
            response.put("results", employeesList);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            response.put("status", "error");
            response.put("message", "Failed to add employee: " + e.getMessage());
        }
        return response;
    }


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
        logger.info("Received user query: " + userQuery + " for database: " + dbType);

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

        logger.info("Sending request to OpenAI: " + jsonRequest);

        RequestBody body = RequestBody.create(jsonRequest, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.severe("OpenAI API Error: " + response.code() + " - " + response.message());
                throw new IOException("Unexpected OpenAI API response: " + response);
            }

            String responseBody = response.body().string();
            logger.info("OpenAI Response: " + responseBody);

            // Extract query from OpenAI response
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            return ((Map<String, String>) ((Map<String, Object>) ((List<?>) result.get("choices")).get(0)).get("message")).get("content");
        }
    }
}
