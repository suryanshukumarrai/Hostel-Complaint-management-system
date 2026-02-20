package com.hostel.controller;

import com.hostel.dto.AuthResponse;
import com.hostel.dto.SignupRequest;
import com.hostel.entity.User;
import com.hostel.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        logger.info("=== Signup Request Received ===");
        logger.info("Username: {}", request.getUsername());
        
        try {
            User user = authService.signup(request);
            logger.info("User registered successfully: {} with role: {}", 
                user.getUsername(), user.getRole());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(
                        "User registered successfully",
                        user.getId(),
                        user.getUsername(),
                        user.getRole()
                    ));
        } catch (Exception e) {
            logger.error("Signup error for username {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(
                        "Signup failed: " + e.getMessage(),
                        null,
                        null,
                        null
                    ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(Authentication authentication) {
        logger.info("=== Login Attempt ===");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Login failed: Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(
                        "Invalid credentials",
                        null,
                        null,
                        null
                    ));
        }
        
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        
        logger.info("Authentication successful for user: {} with authorities: {}", username, authorities);
        
        try {
            User user = authService.findByUsername(username);
            
            logger.info("User {} logged in successfully with role: {}", 
                user.getUsername(), user.getRole());
            
            return ResponseEntity.ok(new AuthResponse(
                "Login successful",
                user.getId(),
                user.getUsername(),
                user.getRole()
            ));
        } catch (Exception e) {
            logger.error("Login error for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(
                        "Login failed: " + e.getMessage(),
                        null,
                        null,
                        null
                    ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        logger.info("=== Get Current User ===");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Get current user failed: Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(
                        "Not authenticated",
                        null,
                        null,
                        null
                    ));
        }
        
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        
        logger.info("Getting current user: {} with authorities: {}", username, authorities);
        
        try {
            User user = authService.findByUsername(username);
            
            logger.info("Current user: {} with role: {}", user.getUsername(), user.getRole());
            
            return ResponseEntity.ok(new AuthResponse(
                "User found",
                user.getId(),
                user.getUsername(),
                user.getRole()
            ));
        } catch (Exception e) {
            logger.error("Get current user error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(
                        "Error retrieving user",
                        null,
                        null,
                        null
                    ));
        }
    }
}
