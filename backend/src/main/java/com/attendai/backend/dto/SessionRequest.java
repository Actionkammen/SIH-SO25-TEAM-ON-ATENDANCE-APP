package com.attendai.backend.dto;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// DTOs
public class SessionRequest {
    @NotBlank(message = "Subject is required")
    private String subject;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Department is required")
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