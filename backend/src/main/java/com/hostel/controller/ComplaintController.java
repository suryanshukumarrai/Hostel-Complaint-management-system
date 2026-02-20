package com.hostel.controller;

import com.hostel.dto.ComplaintDTO;
import com.hostel.dto.CreateComplaintRequest;
import com.hostel.dto.UpdateStatusRequest;
import com.hostel.entity.User;
import com.hostel.service.ComplaintService;
import com.hostel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ComplaintController {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintController.class);

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ComplaintDTO> createComplaint(
            @ModelAttribute CreateComplaintRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        logger.info("=== Create Complaint Request ===");
        logger.info("Category: {}", request.getCategory());
        try {
            ComplaintDTO complaint = complaintService.createComplaint(request, image);
            logger.info("Complaint created successfully with ID: {}", complaint.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(complaint);
        } catch (Exception e) {
            logger.error("Error creating complaint: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<ComplaintDTO>> getAllComplaints(Authentication authentication) {
        logger.info("=== Get All Complaints ===");
        logger.info("User: {}", authentication.getName());
        
        // Get logged-in user
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get user role
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .orElse("CLIENT");
        
        logger.info("User role: {}", role);
        
        // ADMIN sees all complaints, CLIENT sees only their own
        if ("ADMIN".equals(role)) {
            logger.info("Admin user: fetching all complaints");
            return ResponseEntity.ok(complaintService.getAllComplaints());
        } else {
            logger.info("Client user: fetching user's own complaints");
            return ResponseEntity.ok(complaintService.getComplaintsByUser(user));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintDTO> getComplaintById(@PathVariable Long id, Authentication authentication) {
        logger.info("=== Get Complaint By ID ===");
        logger.info("Complaint ID: {}, User: {}", id, authentication.getName());
        
        // Get logged-in user
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get user role
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .findFirst()
                .orElse("CLIENT");
        
        logger.info("User role: {}", role);
        
        return ResponseEntity.ok(complaintService.getComplaintByIdWithAuth(id, user, role));
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ComplaintDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        logger.info("=== Update Complaint Status ===");
        logger.info("Complaint ID: {}, New Status: {}", id, request.getStatus());
        try {
            ComplaintDTO complaint = complaintService.updateStatus(id, request.getStatus());
            logger.info("Complaint status updated successfully");
            return ResponseEntity.ok(complaint);
        } catch (Exception e) {
            logger.error("Error updating complaint status: {}", e.getMessage(), e);
            throw e;
        }
    }
}
