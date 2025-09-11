package com.attendai.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Base64;
import java.util.Map;
import com.attendai.backend.model.ClassSession;
import com.attendai.backend.repository.*;
import com.attendai.backend.service.QRCodeService; 
import jakarta.servlet.http.HttpServletRequest;
import com.attendai.backend.dto.UnifiedRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/unified")
@CrossOrigin(origins = "*")
public class UnifiedApiController {

    @Autowired
    private AuthController authController;

    @Autowired
    private AttendanceController attendanceController;

    @Autowired
    private ClassSessionRepository sessionRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private QRCodeService qrCodeService; // Service to generate QR

    @Autowired
    private ClassSessionController classSessionController;

    /**
     * Unified entry point for all frontend requests
     */
    @PostMapping
    public ResponseEntity<?> handleRequest(
            @RequestBody UnifiedRequest request,
            HttpServletRequest httpRequest) {

        System.out.println("👉 Unified API Called: " + request.getType());
        System.out.println("🔐 Authorization Header: " + httpRequest.getHeader("Authorization"));

        try {
            return switch (request.getType()) {
                case "register" -> authController.register(request.getRegisterRequest());

                case "login" -> authController.login(request.getLoginRequest());

                case "mark-attendance" -> {
                    simulateAuth(request.getEmail());
                    yield attendanceController.markAttendance(request.getAttendanceRequest());
                }

                case "my-attendance" -> {
                    simulateAuth(request.getEmail());
                    yield attendanceController.getMyAttendance();
                }

                case "session-attendance" -> {
                    simulateAuth(request.getEmail());
                    yield attendanceController.getSessionAttendance(request.getSessionId());
                }

                case "student-dashboard" -> {
                    simulateAuth(request.getEmail());
                    yield attendanceController.getStudentDashboard();
                }

                case "faculty-dashboard" -> {
                    simulateAuth(request.getEmail());
                    yield attendanceController.getFacultyDashboard();
                }

                case "weekly-graph" -> {
                    simulateAuth(request.getEmail());
                    yield attendanceController.getWeeklyGraph();
                }

                case "risk-students" -> {
                    simulateAuth(request.getEmail());
                    yield attendanceController.getAtRiskStudents();
                }

                case "create-session" -> classSessionController.createSession(request.getSessionRequest());

                case "qr-code" -> generateQrCodeAsBase64(request.getSessionId());

                default -> ResponseEntity.badRequest().body("Unknown request type: " + request.getType());
                };
            }
         catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        
         }
            }
    // Simulate authenticated user context
    private void simulateAuth(String email) {
        if (email != null && !email.isEmpty()) {
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList())
            );
        }
    }

    // // Create class session
    // private ResponseEntity<?> createClassSession(UnifiedRequest request) {
    //     Optional<User> userOpt = userRepo.findByEmailIgnoreCase(request.getEmail());
    //     if (userOpt.isEmpty() || !userOpt.get().getRole().equals(Role.FACULTY)) {
    //         return ResponseEntity.status(403).body("Only faculty can create sessions");
    //     }

    //     ClassSession session = new ClassSession();
    //     var req = request.getSessionRequest();

    //     session.setSubject(req.getSubject());
    //     session.setFacultyEmail(request.getEmail());
    //     session.setStartTime(req.getStartTime());
    //     session.setEndTime(req.getEndTime());
    //     session.setLocation(req.getLocation());
    //     session.setStatus("ACTIVE");
    //     session.setDepartment(req.getDepartment());

    //     // Generate unique QR token
    //     String qrToken = "SESSION-" + System.currentTimeMillis() + "-" +
    //             java.util.UUID.randomUUID().toString().substring(0, 6);
    //     session.setQrCodeToken(qrToken);

    //     sessionRepo.save(session);

    //     return ResponseEntity.ok("Session created with QR token: " + qrToken);
    // }

    // Generate QR code image
    private ResponseEntity<?> generateQrCodeAsBase64(Long sessionId) {
    Optional<ClassSession> sessionOpt = sessionRepo.findById(sessionId);
    if (sessionOpt.isEmpty()) {
        return ResponseEntity.badRequest().body("Session not found");
    }

    String qrData = "ATTENDAI://" + sessionOpt.get().getQrCodeToken();

    try {
        byte[] imageBytes = qrCodeService.generateQRCodeImage(qrData, 300, 300);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, String> response = new HashMap<>();
        response.put("qrCode", "data:image/png;base64," + base64Image);
        response.put("sessionId", sessionId.toString());
        response.put("message", "QR code generated successfully");

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
}
}