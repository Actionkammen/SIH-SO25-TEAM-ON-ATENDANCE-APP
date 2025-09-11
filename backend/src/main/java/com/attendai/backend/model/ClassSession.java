package com.attendai.backend.model;

import jakarta.persistence.*;

import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(name = "faculty_email", nullable = false)
    private String facultyEmail;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private String location;

    @Column(name = "qr_code_token")
    private String qrCodeToken;

    @Column(nullable = false)
    private String status; // ACTIVE, ENDED

    /**
     * Added to fix: method getDepartment() undefined
     * Used to filter students by department in a session
     */
    @Column(nullable = false)
    private String department; // e.g., "CSE", "ECE", "MECH"

    // Optional: Add course code or semester if needed
    // private String courseCode;
    // private Integer semester;
}