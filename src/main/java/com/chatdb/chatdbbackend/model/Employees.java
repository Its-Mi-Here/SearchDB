package com.chatdb.chatdbbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.Date;
import lombok.Data;

@Entity
@Data
public class Employees {
    @Id
    private int employee_id;
    private String first_name;
    private String last_name;
    private String email;
    private String job_title;
    private Date hire_date;
}
