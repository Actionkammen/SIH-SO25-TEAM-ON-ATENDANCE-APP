package com.attendai.backend.dto;

import lombok.Data;

@Data
public class ClassStudentDTO {
    private String rollNumber;
    private String name;
    private boolean present;
    private String method; // "QR", "FACIAL", etc.

    public ClassStudentDTO(String rollNumber, String name, boolean present, String method) {
        this.rollNumber = rollNumber;
        this.name = name;
        this.present = present;
        this.method = method;
    }
}