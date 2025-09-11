package com.attendai.backend.dto;

import lombok.Data;

@Data
public class AtRiskStudent {
    private String rollNumber;
    private String subject;
    private double attendance;

    public AtRiskStudent(String rollNumber, String subject, double attendance) {
        this.rollNumber = rollNumber;
        this.subject = subject;
        this.attendance = attendance;
    }
}