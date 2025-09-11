package com.attendai.backend.controller;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendai.backend.dto.SessionRequest;
import com.attendai.backend.model.ClassSession;
import com.attendai.backend.model.Role;
import com.attendai.backend.model.User;
import com.attendai.backend.repository.ClassSessionRepository;
import com.attendai.backend.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/session")
@CrossOrigin(origins = "http://localhost:3000")
public class ClassSessionController {

    @Autowired
    private ClassSessionRepository sessionRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * Create a new class session
     * POST /api/session/create
     */
    //@PostMapping("/create")
    // public ResponseEntity<?> createSession(
    //     @Valid @RequestBody SessionRequest request) {
    //     // Get current authenticated user (faculty)
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     String email = auth.getName();

    //     // Optional: Verify user is faculty
    //     Optional<User> userOpt = userRepo.findByEmailIgnoreCase(email);
    //     if (userOpt.isEmpty() || !userOpt.get().getRole().equals(Role.FACULTY)) {
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only faculty can create sessions");
    //     }
    //     @PostMapping("/create")
    // public ResponseEntity<?> createSession(@RequestBody SessionRequest request) {
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     System.out.println("🔐 Authenticated User: " + auth.getName());
    //     String email = auth.getName();
    //     Optional<User> userOpt = userRepo.findByEmailIgnoreCase(auth.getName());
    //     if (userOpt.isEmpty()) {
    //         return ResponseEntity.status(404).body("User not found");
    //     }

    //     User user = userOpt.get();
    //     System.out.println("👤 User Role: " + user.getRole());

    //     if (!Role.FACULTY.equals(user.getRole())) {
    //         System.out.println("❌ Access denied: Expected FACULTY, got " + user.getRole());
    //         return ResponseEntity.status(403).body("Only faculty can create sessions");
    //     }
    //     // Create session
    //     ClassSession session = new ClassSession();
    //     session.setSubject(request.getSubject());
    //     session.setFacultyEmail(email);
    //     session.setStartTime(request.getStartTime());
    //     session.setEndTime(request.getEndTime());
    //     session.setLocation(request.getLocation());
    //     session.setStatus("ACTIVE");
    //     session.setDepartment(request.getDepartment());

    //     // Generate unique QR token
    //     String qrToken = "SESSION-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6);
    //     session.setQrCodeToken(qrToken);

    //     sessionRepo.save(session);

    //     return ResponseEntity.ok("Session created successfully with QR token: " + qrToken);
    // }
        @PostMapping("/create")
    public ResponseEntity<?> createSession(@Valid @RequestBody SessionRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔐 Authenticated User: " + auth.getName());

        Optional<User> userOpt = userRepo.findByEmailIgnoreCase(auth.getName());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();
        System.out.println("👤 User Role: " + user.getRole());
        System.out.println("📧 User Department: " + user.getDepartment()); // Debug

        if (!Role.FACULTY.equals(user.getRole())) {
            return ResponseEntity.status(403).body("Only faculty can create sessions");
        }

        ClassSession session = new ClassSession();
        session.setSubject(request.getSubject());
        session.setFacultyEmail(auth.getName());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setLocation(request.getLocation());
        session.setStatus("ACTIVE");

        // ✅ Use request.department, fallback to user's department
        String dept = request.getDepartment();
        if (dept == null || dept.isBlank()) {
            dept = user.getDepartment(); // Fallback
        }
        session.setDepartment(dept);

        String qrToken = "SESSION-" + System.currentTimeMillis() + "-" +
                java.util.UUID.randomUUID().toString().substring(0, 6);
        session.setQrCodeToken(qrToken);

        sessionRepo.save(session);

        return ResponseEntity.ok("Session created successfully with ID: " + session.getId());
    }
    /**
     * Generate QR code for a session
     * GET /api/session/qr/{sessionId}
     */
    @GetMapping("/qr/{sessionId}")
    public ResponseEntity<byte[]> generateQRCode(@PathVariable Long sessionId) throws Exception {
        Optional<ClassSession> sessionOpt = sessionRepo.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ClassSession session = sessionOpt.get();

        // QR data format: ATTENDAI://SESSION-123456789-abc123
        String qrData = "ATTENDAI://" + session.getQrCodeToken();

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] imageBytes = pngOutputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=qr-session-" + sessionId + ".png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);

        } catch (WriterException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * End a session
     * POST /api/session/end/{sessionId}
     */
    @PostMapping("/end/{sessionId}")
    public ResponseEntity<?> endSession(@PathVariable Long sessionId) {
        Optional<ClassSession> sessionOpt = sessionRepo.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Session not found");
        }

        ClassSession session = sessionOpt.get();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!session.getFacultyEmail().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        session.setStatus("ENDED");
        sessionRepo.save(session);

        return ResponseEntity.ok("Session ended successfully");
    }
}




