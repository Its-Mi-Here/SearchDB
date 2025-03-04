package com.chatdb.chatdbbackend.repository;

import com.chatdb.chatdbbackend.model.MongoUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoUserRepository extends MongoRepository<MongoUser, String> {
}
