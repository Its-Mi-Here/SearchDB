package com.chatdb.chatdbbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ChatDbJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatDbJavaApplication.class, args);
    }

}