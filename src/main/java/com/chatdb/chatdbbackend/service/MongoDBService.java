package com.chatdb.chatdbbackend.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    private Object executeInsertOne(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);
        Document document = extractJsonFromQuery(query);
        database.getCollection(collectionName).insertOne(document);
        return "Inserted successfully into " + collectionName;
    }

    private Object executeInsertMany(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);
        List<Document> documents = extractJsonArrayFromQuery(query);
        database.getCollection(collectionName).insertMany(documents);
        return "Inserted multiple documents into " + collectionName;
    }

    private Object executeFind(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);
        Document filter = extractJsonFromQuery(query);
        return database.getCollection(collectionName).find(filter).into(new java.util.ArrayList<>());
    }

    private Object executeAggregate(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);
        List<Document> pipeline = extractJsonArrayFromQuery(query);
        return database.getCollection(collectionName).aggregate(pipeline).into(new java.util.ArrayList<>());
    }

    private Object executeDeleteOne(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);
        Document filter = extractJsonFromQuery(query);
        database.getCollection(collectionName).deleteOne(filter);
        return "Deleted one document from " + collectionName;
    }

//    private Object executeUpdateOne(String query, MongoDatabase database) {
//        String collectionName = extractCollectionName(query);
//        Document updateQuery = extractJsonFromQuery(query);
//        database.getCollection(collectionName).updateOne(updateQuery, updateQuery);
//        return "Updated one document in " + collectionName;
//    }
    private Object executeUpdateOne(String query, MongoDatabase database) {
        String collectionName = extractCollectionName(query);

        // Extract filter condition and update operation
        String[] parts = query.split("\\),\\s*\\{");
        if (parts.length < 2) {
            return "Invalid updateOne syntax. Expected format: db.collection.updateOne({filter}, {update})";
        }

        String filterJson = parts[0].substring(parts[0].indexOf("(") + 1).trim();
        String updateJson = "{" + parts[1].trim(); // Ensure the update operation starts with '{'

        Document filter = Document.parse(filterJson);
        Document update = Document.parse(updateJson);

        database.getCollection(collectionName).updateOne(filter, update);
        return "Updated one document in " + collectionName;
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
}
