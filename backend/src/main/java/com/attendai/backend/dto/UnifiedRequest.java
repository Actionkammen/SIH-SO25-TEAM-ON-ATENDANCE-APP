package com.attendai.backend.dto;
public class UnifiedRequest {
    private String type;
    private String email;
    private Long sessionId;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AttendanceRequest attendanceRequest;
    private SessionRequest sessionRequest;

    // Getters & Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public RegisterRequest getRegisterRequest() { return registerRequest; }
    public void setRegisterRequest(RegisterRequest registerRequest) { this.registerRequest = registerRequest; }

    public LoginRequest getLoginRequest() { return loginRequest; }
    public void setLoginRequest(LoginRequest loginRequest) { this.loginRequest = loginRequest; }

    public AttendanceRequest getAttendanceRequest() { return attendanceRequest; }
    public void setAttendanceRequest(AttendanceRequest attendanceRequest) { this.attendanceRequest = attendanceRequest; }

    public SessionRequest getSessionRequest() { return sessionRequest; }
    public void setSessionRequest(SessionRequest sessionRequest) { this.sessionRequest = sessionRequest; }
}