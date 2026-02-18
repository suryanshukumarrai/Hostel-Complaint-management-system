package com.hostel.controller;

import com.hostel.dto.AuthResponse;
import com.hostel.dto.SignupRequest;
import com.hostel.entity.User;
import com.hostel.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        System.out.println("Signup request received: " + request.getUsername());
        try {
            User user = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse("User registered successfully", user.getId(), user.getUsername(), user.getRole()));
        } catch (Exception e) {
            System.err.println("Signup error: " + e.getMessage());
            e.printStackTrace();
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
