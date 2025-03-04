package com.chatdb.chatdbbackend.controller;
import com.chatdb.chatdbbackend.model.QueryRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/query")
@CrossOrigin(origins = "*")
public class QueryController {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/execute")
    public List<Map<String, Object>> executeQuery(@RequestBody QueryRequest request) {
        return jdbcTemplate.queryForList(request.getQuery());
    }
}
