package com.attendai.backend.dto;

import lombok.Data;

@Data
public class ClassSummary {
    private String subject;
    private String startTime;
    private String endTime;
    private String location;
    private String status;

    public ClassSummary(String subject, String startTime, String endTime, String location, String status) {
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.status = status;
    }
}