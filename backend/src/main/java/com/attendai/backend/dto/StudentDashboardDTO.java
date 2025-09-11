package com.attendai.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class StudentDashboardDTO {
    private String name;
    private String rollNumber;
    private String department;
    private double overallAttendance;
    private long totalClasses;
    private long present;
    private long absent;
    private List<SubjectAttendance> subjects;
}