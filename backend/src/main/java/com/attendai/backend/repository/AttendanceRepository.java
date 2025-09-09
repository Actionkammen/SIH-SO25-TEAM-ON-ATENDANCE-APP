package com.attendai.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.attendai.backend.model.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByRollNumber(String rollNumber);
    List<Attendance> findBySessionId(Long sessionId);
    Optional<Attendance> findByRollNumberAndSessionId(String rollNumber, Long sessionId);
}
