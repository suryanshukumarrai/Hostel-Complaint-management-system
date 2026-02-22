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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        logger.info("Signup request received for username: {}", request.getUsername());
        try {
            User user = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse("User registered successfully", user.getId(), user.getUsername(), user.getRole()));
        } catch (Exception e) {
            logger.error("Signup failed for username: {}", request.getUsername(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = authService.findByUsername(authentication.getName());
            return ResponseEntity.ok(new AuthResponse("Login successful", user.getId(), user.getUsername(), user.getRole()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse("Invalid credentials", null, null, null));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = authService.findByUsername(authentication.getName());
            return ResponseEntity.ok(new AuthResponse("User found", user.getId(), user.getUsername(), user.getRole()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
