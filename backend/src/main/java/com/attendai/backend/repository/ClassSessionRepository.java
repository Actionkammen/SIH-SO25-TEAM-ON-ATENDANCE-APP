package com.attendai.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.attendai.backend.model.ClassSession;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
}
