package com.attendai.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.attendai.backend.model.ClassSession;


public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
    List<ClassSession> findByFacultyEmailAndStartTimeBetween(
        String facultyEmail,
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );
}