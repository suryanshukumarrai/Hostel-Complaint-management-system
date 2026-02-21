package com.hostel.controller;

import com.hostel.dto.ComplaintDTO;
import com.hostel.dto.CreateComplaintRequest;
import com.hostel.dto.UpdateStatusRequest;
import com.hostel.entity.User;
import com.hostel.service.ComplaintService;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ComplaintDTO> createComplaint(
            @ModelAttribute CreateComplaintRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        ComplaintDTO complaint = complaintService.createComplaint(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(complaint);
    }

    @GetMapping
    public ResponseEntity<List<ComplaintDTO>> getAllComplaints(Authentication authentication) {
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
        
        // ADMIN sees all complaints, CLIENT sees only their own
        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(complaintService.getAllComplaints());
        } else {
            return ResponseEntity.ok(complaintService.getComplaintsByUser(user));
        }
    }

            @GetMapping("/search")
            public ResponseEntity<List<ComplaintDTO>> searchComplaints(
                Authentication authentication,
                @RequestParam(value = "q", required = false) String query,
                @RequestParam(value = "agent", required = false) String agent,
                @RequestParam(value = "fromDate", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                @RequestParam(value = "toDate", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                @RequestParam(value = "category", required = false) com.hostel.entity.Category category
            ) {
            User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .findFirst()
                .orElse("CLIENT");

            List<ComplaintDTO> result = complaintService.searchComplaints(query, agent, fromDate, toDate, category, user, role);
            return ResponseEntity.ok(result);
            }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintDTO> getComplaintById(@PathVariable Long id, Authentication authentication) {
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
        
        return ResponseEntity.ok(complaintService.getComplaintByIdWithAuth(id, user, role));
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ComplaintDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(complaintService.updateStatus(id, request.getStatus()));
    }
}
