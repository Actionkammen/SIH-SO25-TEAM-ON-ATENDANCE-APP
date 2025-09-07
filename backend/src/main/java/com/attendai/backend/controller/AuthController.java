package com.attendai.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.attendai.backend.config.JwtUtil;
import com.attendai.backend.dto.JwtResponse;
import com.attendai.backend.dto.LoginRequest;
import com.attendai.backend.dto.RegisterRequest;
import com.attendai.backend.model.Role;
import com.attendai.backend.model.User;
import com.attendai.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080")
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Check if email already exists
        if (userRepo.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRollNumber(request.getRollNumber());
        user.setDepartment(request.getDepartment());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole()));

        // Save to database
        userRepo.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest request) {
            System.out.println("👉 Login Attempt: " + request.getEmail());

            try {
                var authToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
                authManager.authenticate(authToken);
                System.out.println("✅ Authentication SUCCESS");

                User userDetails = userRepo.findByEmailIgnoreCase(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("User not found in DB"));

                String token = jwtUtil.generateToken(userDetails);
                return ResponseEntity.ok(new JwtResponse(token, "ROLE_" + userDetails.getRole(), userDetails.getName()));

            } catch (BadCredentialsException e) {
                System.out.println("❌ Bad Credentials: " + e.getMessage());
                return ResponseEntity.status(403).body("Invalid email or password");
            } catch (Exception e) {
                System.out.println("❌ Other Error: " + e.getMessage());
                return ResponseEntity.status(500).body("Login failed: " + e.getMessage());
        }
    }
}