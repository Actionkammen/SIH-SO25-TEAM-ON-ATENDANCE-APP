package com.attendai.backend.dto;
import java.time.LocalDateTime;
// DTOs
public class SessionRequest {
    private String subject;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String department;

    // Getters & Setters
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}