package com.hostel.controller;

import com.hostel.dto.ComplaintDTO;
import com.hostel.dto.CreateComplaintRequest;
import com.hostel.dto.UpdateStatusRequest;
import com.hostel.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @PostMapping
    public ResponseEntity<ComplaintDTO> createComplaint(@RequestBody CreateComplaintRequest request) {
        ComplaintDTO complaint = complaintService.createComplaint(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(complaint);
    }

    @GetMapping
    public ResponseEntity<List<ComplaintDTO>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComplaintDTO> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintById(id));
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ComplaintDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(complaintService.updateStatus(id, request.getStatus()));
    }
}
