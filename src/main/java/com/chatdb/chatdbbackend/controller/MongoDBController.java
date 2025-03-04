package com.chatdb.chatdbbackend.controller;

import com.chatdb.chatdbbackend.service.MongoDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mongo")
@CrossOrigin(origins = "*")
public class MongoDBController {
    @Autowired
    private MongoDBService mongoDBService;

    @PostMapping("/execute")
    public Object executeMongoQuery(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        return mongoDBService.executeMongoCommand(query);
    }
}
