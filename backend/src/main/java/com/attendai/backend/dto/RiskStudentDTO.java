package com.attendai.backend.dto;

import lombok.Data;

@Data
public class RiskStudentDTO {
    private String rollNumber;
    private String name;
    private double overallAttendance;
    private String department;
}