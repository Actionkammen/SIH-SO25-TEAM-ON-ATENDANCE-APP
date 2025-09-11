package com.attendai.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class FacultyDashboardDTO {
    private String name;
    private String department;
    private List<ClassSummary> todayClasses;
    private int activeSessions;
    private long totalAttendanceToday;
    private List<AtRiskStudent> atRiskStudents;
}