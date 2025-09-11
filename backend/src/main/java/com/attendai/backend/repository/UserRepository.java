package com.attendai.backend.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.attendai.backend.model.Role;
import com.attendai.backend.model.User;



public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    List<User> findByRole(Role role); // Add this

    List<User> findByDepartmentAndRole(String department, Role role);
}