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

import okhttp3.*;
import org.springframework.jdbc.core.JdbcTemplate;


@Service
public class MainService {

    private final MongoDBService mongoDBService;

    private final JdbcTemplate jdbcTemplate;

    final
    EmployeeRepo repo;

    private final KafkaProducerService kafkaProducerService;

    private final OpenAIService openAIService;

    public MainService(MongoDBService mongoDBService, JdbcTemplate jdbcTemplate, EmployeeRepo repo, KafkaProducerService kafkaProducerService, OpenAIService openAIService) {
        this.mongoDBService = mongoDBService;
        this.jdbcTemplate = jdbcTemplate;
        this.repo = repo;
        this.kafkaProducerService = kafkaProducerService;
        this.openAIService = openAIService;
    }

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
            employee.setFirst_name((String) entry.get("first_name") );
            employee.setLast_name((String) entry.get("last_name") );
            employee.setEmail((String) entry.get("email") );
            employee.setJob_title( (String) entry.get("job_title") );

            String hireDateString = (String) entry.get("hire_date");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date hireDate = dateFormat.parse(hireDateString);
            employee.setHire_date(hireDate);
            repo.save(employee);

            List<Employees> employeesList = repo.findAll();
            response.put("status", "success");
            response.put("results", employeesList);

            kafkaProducerService.publishEntryToDB("Notification: User added - {name: "+ entry.get("first_name") + " " + entry.get("last_name") + "}" );

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
            String generatedQuery = openAIService.generateQuery(userQuery, dbType);

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

}
