package com.chatdb.chatdbbackend.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class MongoDBService {

    private static final Logger logger = Logger.getLogger(MongoDBService.class.getName());

    @Autowired
    private MongoClient mongoClient;

    public Object executeMongoCommand(String query) {
        MongoDatabase database = mongoClient.getDatabase("chatdb_mongo");

        try {
            // Detect the command type
            if (query.startsWith("db.")) {
                if (query.contains(".insertOne(")) {
                    return executeInsertOne(query, database);
                } else if (query.contains(".insertMany(")) {
                    return executeInsertMany(query, database);
                } else if (query.contains(".find(")) {
                    return executeFind(query, database);
                } else if (query.contains(".aggregate(")) {
                    return executeAggregate(query, database);
                } else if (query.contains(".deleteOne(")) {
                    return executeDeleteOne(query, database);
                } else if (query.contains(".updateOne(")) {
                    return executeUpdateOne(query, database);
                }
            }
            return "Unsupported MongoDB command.";
        } catch (Exception e) {
            logger.severe("MongoDB Query Execution Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private List<Map<String, Object>> executeInsertOne(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);
        Document document = extractJsonFromQuery(query);
        database.getCollection(collectionName).insertOne(document);

        return generateSuccessResponse("Inserted successfully into " + collectionName);
    }

    private List<Map<String, Object>> executeInsertMany(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);
        List<Document> documents = extractJsonArrayFromQuery(query);
        database.getCollection(collectionName).insertMany(documents);

        return generateSuccessResponse("Inserted multiple documents into " + collectionName);
    }

    private List<Map<String, Object>> executeFind(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);
        Document filter = extractJsonFromQuery(query);
        List<Map<String, Object>> results = new ArrayList<>();

        for (Document doc : database.getCollection(collectionName).find(filter)) {
            results.add(docToMap(doc));
        }

        return results;
    }

    private List<Map<String, Object>> executeAggregate(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);
        List<Document> pipeline = extractJsonArrayFromQuery(query);
        List<Map<String, Object>> results = new ArrayList<>();

        for (Document doc : database.getCollection(collectionName).aggregate(pipeline)) {
            results.add(docToMap(doc));
        }

        return results;
    }

    private List<Map<String, Object>> executeDeleteOne(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);
        Document filter = extractJsonFromQuery(query);
        database.getCollection(collectionName).deleteOne(filter);

        return generateSuccessResponse("Deleted one document from " + collectionName);
    }

    private List<Map<String, Object>> executeUpdateOne(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);

        // Extract filter condition and update operation
        String[] parts = query.split("\\),\\s*\\{");
        if (parts.length < 2) {
            return generateErrorResponse("Invalid updateOne syntax. Expected format: db.collection.updateOne({filter}, {update})");
        }

        String filterJson = parts[0].substring(parts[0].indexOf("(") + 1).trim();
        String updateJson = "{" + parts[1].trim(); // Ensure the update operation starts with '{'

        Document filter = Document.parse(filterJson);
        Document update = Document.parse(updateJson);

        database.getCollection(collectionName).updateOne(filter, update);

        return generateSuccessResponse("Updated one document in " + collectionName);
    }

    private String extractCollectionName(String query) {
        return query.split("\\.")[1].split("\\(")[0];
    }

    private Document extractJsonFromQuery(String query) {
        String json = query.substring(query.indexOf("(") + 1, query.lastIndexOf(")"));
        return Document.parse(json);
    }

    private List<Document> extractJsonArrayFromQuery(String query) {
        String jsonArray = query.substring(query.indexOf("[") + 1, query.lastIndexOf("]"));
        return List.of(Document.parse("[" + jsonArray + "]"));
    }

    private Map<String, Object> docToMap(Document doc) {
        if (doc == null) return Collections.emptyMap();

        Map<String, Object> map = new HashMap<>(doc);

        if (map.containsKey("_id") && map.get("_id") instanceof org.bson.types.ObjectId) {
            map.put("_id", map.get("_id").toString());
        }

        return map;
    }

    private List<Map<String, Object>> generateSuccessResponse(String message) {
        List<Map<String, Object>> response = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", message);
        response.add(result);
        return response;
    }

    private List<Map<String, Object>> generateErrorResponse(String message) {
        List<Map<String, Object>> response = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", message);
        response.add(result);
        return response;
    }
}