package com.attendai.backend.controller;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.attendai.backend.repository.AttendanceRepository;
import com.attendai.backend.repository.UserRepository;
import com.attendai.backend.dto.UnifiedRequest; 

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/unified")
@CrossOrigin(origins = "*")
public class UnifiedApiController {

    @Autowired
    private AuthController authController;

    @Autowired
    private AttendanceController attendanceController;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AttendanceRepository attendanceRepo;

    @PostMapping
    public ResponseEntity<?> handleRequest(@RequestBody UnifiedRequest request) {
        try {
            return switch (request.getType()) {
                case "register" -> authController.register(request.getRegisterRequest());
                case "login" -> authController.login(request.getLoginRequest());
                case "mark-attendance" -> {
                    // Simulate authenticated context
                    SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                            request.getEmail(), null, Collections.emptyList()
                        )
                    );
                    yield attendanceController.markAttendance(request.getAttendanceRequest());
                }
                case "my-attendance" -> {
                    SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                            request.getEmail(), null, Collections.emptyList()
                        )
                    );
                    yield attendanceController.getMyAttendance();
                }
                case "session-attendance" -> {
                    SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                            request.getEmail(), null, Collections.emptyList()
                        )
                    );
                    yield attendanceController.getSessionAttendance(request.getSessionId());
                }
                default -> ResponseEntity.badRequest().body("Unknown request type");
            };
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}