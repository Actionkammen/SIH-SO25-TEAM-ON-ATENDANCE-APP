package com.attendai.backend.dto;

import lombok.Data;

@Data
public class SubjectAttendance {
    private String subject;
    private double attendance;
    private String status; // "Good", "At Risk"

    public SubjectAttendance(String subject, double attendance, String status) {
        this.subject = subject;
        this.attendance = attendance;
        this.status = status;
    }
}