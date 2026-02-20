package com.hostel.controller;

import com.hostel.ai.AiService;
import com.hostel.dto.AiComplaintRequest;
import com.hostel.dto.AiComplaintResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    @Autowired
    private AiService aiService;

    @Autowired
    private com.hostel.repository.UserRepository userRepository;

    /**
     * Generate complaint from free-form description using AI
     * 
     * POST /api/ai/generate-complaint
     * Requires: CLIENT role
     * 
     * Request Body:
     * {
     *   "description": "My water tap is broken and..."
     * }
     * 
     * Response:
     * {
     *   "id": 123,
     *   "category": "PLUMBING",
     *   "subCategory": "Tap Issue",
     *   "roomNo": "A401",
     *   "priority": "HIGH",
     *   "status": "OPEN",
     *   "description": "...",
     *   "message": "Complaint generated successfully"
     * }
     */
    @PostMapping("/generate-complaint")
    public ResponseEntity<?> generateComplaint(
            @RequestBody AiComplaintRequest request,
            Authentication authentication) {
        
        // Debug: Log authentication details
        logger.info("=== AI Complaint Generation Request ===");
        logger.info("Authenticated: {}", authentication != null && authentication.isAuthenticated());
        logger.info("User: {}", authentication != null ? authentication.getName() : "NULL");
        logger.info("Authorities: {}", authentication != null ? authentication.getAuthorities() : "NULL");
        
        // Check authorization with detailed logging
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Access denied: User not authenticated");
            return ResponseEntity.status(401)
                .body(new ErrorResponse("Authentication required", 401));
        }
        
        // Get user's authorities
        String authoritiesStr = authentication.getAuthorities().stream()
            .map(auth -> auth.getAuthority())
            .reduce((a, b) -> a + ", " + b)
            .orElse("NONE");
        
        logger.info("User authorities: {}", authoritiesStr);
        
        // Check for CLIENT role (flexible - accept both "ROLE_CLIENT" and "CLIENT")
        boolean hasClientRole = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_CLIENT") || auth.getAuthority().equals("CLIENT"));
        
        if (!hasClientRole) {
            logger.warn("Access denied: User {} does not have CLIENT role. Has authorities: {}", 
            authentication.getName(), authoritiesStr);
            return ResponseEntity.status(403)
                .body(new ErrorResponse(
                "Access denied: You do not have CLIENT role. Your authorities: " + authoritiesStr,
                403));
        }
        
        logger.info("Authorization passed for user: {}", authentication.getName());
        logger.info("Received AI complaint generation request from user: {}", authentication.getName());

        // Validate input
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            logger.warn("Empty description provided");
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Description cannot be empty", 400));
        }

        if (request.getDescription().length() > 10000) {
            logger.warn("Description too long: {} characters", request.getDescription().length());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Description cannot exceed 10000 characters", 400));
        }

        // Get user ID
        com.hostel.entity.User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate complaint
        logger.info("Processing AI complaint generation for user ID: {}", user.getId());
        AiComplaintResponse response = aiService.generateComplaintFromDescription(
            request.getDescription(),
            user.getId()
        );

        logger.info("Successfully generated complaint with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Error response DTO
     */
    private static class ErrorResponse {
        private String message;
        private int status;

        public ErrorResponse(String message, int status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
