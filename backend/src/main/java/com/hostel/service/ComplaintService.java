package com.hostel.service;

import com.hostel.dto.ComplaintDTO;
import com.hostel.dto.CreateComplaintRequest;
import com.hostel.dto.UserDTO;
import com.hostel.entity.Category;
import com.hostel.entity.Complaint;
import com.hostel.entity.Status;
import com.hostel.entity.User;
import com.hostel.exception.ResourceNotFoundException;
import com.hostel.repository.ComplaintRepository;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private UserRepository userRepository;

    public ComplaintDTO createComplaint(CreateComplaintRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Complaint complaint = new Complaint();
        complaint.setMessageType(request.getMessageType());
        complaint.setCategory(request.getCategory());
        complaint.setSubCategory(request.getSubCategory());
        complaint.setSpecificCategory(request.getSpecificCategory());
        complaint.setBlock(request.getBlock());
        complaint.setSubBlock(request.getSubBlock());
        complaint.setRoomType(request.getRoomType());
        complaint.setRoomNo(request.getRoomNo());
        complaint.setContactNo(request.getContactNo());
        complaint.setTimeSlot(request.getTimeSlot());
        complaint.setDescription(request.getDescription());
        complaint.setRaisedBy(user);
        complaint.setStatus(Status.OPEN);
        complaint.setAssignedTo(resolveAssignee(request.getCategory()));

        if (request.getAvailabilityDate() != null && !request.getAvailabilityDate().isBlank()) {
            complaint.setAvailabilityDate(LocalDate.parse(request.getAvailabilityDate()));
        }

        Complaint saved = complaintRepository.save(complaint);
        return convertToDTO(saved);
    }

    public List<ComplaintDTO> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ComplaintDTO> getComplaintsByUser(User user) {
        return complaintRepository.findByRaisedBy(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ComplaintDTO getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        return convertToDTO(complaint);
    }

    public ComplaintDTO getComplaintByIdWithAuth(Long id, User user, String role) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        
        // If CLIENT role, verify they own this complaint
        if ("CLIENT".equals(role) && !complaint.getRaisedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You can only view your own complaints");
        }
        
        return convertToDTO(complaint);
    }

    public ComplaintDTO updateStatus(Long id, Status status) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
        complaint.setStatus(status);
        return convertToDTO(complaintRepository.save(complaint));
    }

    private String resolveAssignee(Category category) {
        if (category == null) return "Unassigned";
        return switch (category) {
            case CARPENTRY -> "Ram";
            case RAGGING -> "Shyam";
            case ELECTRICAL -> "Electric Team";
            case PLUMBING -> "Plumber Team";
        };
    }

    private ComplaintDTO convertToDTO(Complaint c) {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(c.getId());
        dto.setMessageType(c.getMessageType());
        dto.setCategory(c.getCategory());
        dto.setSubCategory(c.getSubCategory());
        dto.setSpecificCategory(c.getSpecificCategory());
        dto.setBlock(c.getBlock());
        dto.setSubBlock(c.getSubBlock());
        dto.setRoomType(c.getRoomType());
        dto.setRoomNo(c.getRoomNo());
        dto.setContactNo(c.getContactNo());
        dto.setAvailabilityDate(c.getAvailabilityDate());
        dto.setTimeSlot(c.getTimeSlot());
        dto.setDescription(c.getDescription());
        dto.setAssignedTo(c.getAssignedTo());
        dto.setStatus(c.getStatus());
        dto.setCreatedAt(c.getCreatedAt());

        User u = c.getRaisedBy();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(u.getId());
        userDTO.setName(u.getFullName());
        userDTO.setRole(u.getRole());
        dto.setRaisedBy(userDTO);

        return dto;
    }
}
