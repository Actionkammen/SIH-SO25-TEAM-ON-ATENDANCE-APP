package com.attendai.backend.dto;

public class AttendanceRequest {
    private Long sessionId;
    private String method;

    // Getters & Setters
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}
