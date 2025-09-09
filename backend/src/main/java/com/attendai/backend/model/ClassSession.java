package com.attendai.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "class_session")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class ClassSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    private String facultyEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location; // "Room 205" or "Google Meet"
    private String qrCodeToken; // Unique per session
    private String status; // "ACTIVE", "ENDED"
}

