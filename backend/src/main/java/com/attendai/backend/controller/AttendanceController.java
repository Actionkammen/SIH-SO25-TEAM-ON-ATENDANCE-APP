package com.attendai.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.attendai.backend.dto.AttendanceRequest;
import com.attendai.backend.model.Attendance;
import com.attendai.backend.model.ClassSession;
import com.attendai.backend.model.User;
import com.attendai.backend.repository.AttendanceRepository;
import com.attendai.backend.repository.ClassSessionRepository;
import com.attendai.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "http://localhost:3000")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepo;

    @Autowired
    private ClassSessionRepository sessionRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * Mark attendance for the authenticated student
     * Method: POST /api/attendance/mark
     * Body: { "sessionId": 1, "method": "QR" }
     */
    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(
            @RequestBody AttendanceRequest request) {

        // Get authenticated user (email) from JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String studentEmail = auth.getName();

        // Fetch user to get roll number
        Optional<User> userOpt = userRepo.findByEmailIgnoreCase(studentEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        String rollNumber = userOpt.get().getRollNumber();

        // Validate session
        Optional<ClassSession> sessionOpt = sessionRepo.findById(request.getSessionId());
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Session not found");
        }
        ClassSession session = sessionOpt.get();

        if (!"ACTIVE".equals(session.getStatus())) {
            return ResponseEntity.badRequest().body("Session is not active");
        }

        // Prevent duplicate marking
        Optional<Attendance> existing = attendanceRepo
                .findByRollNumberAndSessionId(rollNumber, request.getSessionId());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Attendance already marked for Roll No: " + rollNumber);
        }

        // Save new attendance record
        Attendance attendance = new Attendance();
        attendance.setRollNumber(rollNumber);
        attendance.setSessionId(request.getSessionId());
        attendance.setTimestamp(LocalDateTime.now());
        attendance.setPresent(true);
        attendance.setMethod(request.getMethod()); // "QR", "FACIAL", "MANUAL"

        attendanceRepo.save(attendance);

        return ResponseEntity.ok("✅ Attendance marked successfully for Roll No: " + rollNumber);
    }

    /**
     * View own attendance history (Student)
     * GET /api/attendance/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<Attendance>> getMyAttendance() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Optional<User> userOpt = userRepo.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        String rollNumber = userOpt.get().getRollNumber();

        List<Attendance> attendanceList = attendanceRepo.findByRollNumber(rollNumber);
        return ResponseEntity.ok(attendanceList);
    }

    /**
     * View attendance for a session (Faculty only)
     * GET /api/attendance/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSessionAttendance(@PathVariable Long sessionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String facultyEmail = auth.getName();

        Optional<ClassSession> sessionOpt = sessionRepo.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Session not found");
        }

        ClassSession session = sessionOpt.get();
        if (!session.getFacultyEmail().equals(facultyEmail)) {
            return ResponseEntity.status(403).body("❌ Access denied: You are not the faculty for this session");
        }

        List<Attendance> attendanceList = attendanceRepo.findBySessionId(sessionId);
        return ResponseEntity.ok(attendanceList);
    }

    /**
     * View attendance by roll number (Admin/Faculty)
     * GET /api/attendance/student/{rollNumber}
     */
    @GetMapping("/student/{rollNumber}")
    public ResponseEntity<List<Attendance>> getAttendanceByStudent(
            @PathVariable String rollNumber) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepo.findByEmailIgnoreCase(auth.getName()).orElse(null);

        // Only Admin and Faculty can view others' attendance
        if (currentUser != null && "STUDENT".equals(currentUser.getRole())) {
            // Student can only view their own if roll number matches
            String myRollNumber = currentUser.getRollNumber();
            if (!myRollNumber.equals(rollNumber)) {
                return ResponseEntity.status(403).body(null);
            }
        }

        List<Attendance> attendanceList = attendanceRepo.findByRollNumber(rollNumber);
        return ResponseEntity.ok(attendanceList);
    }
}