package com.hostel.controller;

import com.hostel.dto.AiGenerateComplaintRequest;
import com.hostel.dto.ComplaintDTO;
import com.hostel.service.AiComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiComplaintController {

    @Autowired
    private AiComplaintService aiComplaintService;

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/generate-complaint")
    public ResponseEntity<?> generateComplaint(
            @RequestBody AiGenerateComplaintRequest request,
            Authentication authentication) {
        if (request == null || request.getDescription() == null || request.getDescription().isBlank()) {
            return ResponseEntity.badRequest().body("Description is required");
        }
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        try {
            ComplaintDTO created = aiComplaintService.generateComplaint(request.getDescription(), authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("AI complaint generation failed");
        }
    }
}
