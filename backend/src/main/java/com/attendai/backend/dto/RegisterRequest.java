package com.attendai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private String rollNumber;
    private String department;
    private String password;
    private String role; // "STUDENT", "FACULTY", "ADMIN"
}
