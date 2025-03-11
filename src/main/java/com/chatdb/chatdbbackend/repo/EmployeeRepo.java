package com.chatdb.chatdbbackend.repo;

import com.chatdb.chatdbbackend.model.Employees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepo extends JpaRepository<Employees, Integer> {

}
