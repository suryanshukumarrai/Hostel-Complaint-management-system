package com.hostel.service;

import com.hostel.dto.StructuredComplaintData;
import com.hostel.entity.Category;
import com.hostel.entity.Complaint;
import com.hostel.entity.MessageType;
import com.hostel.entity.Status;
import com.hostel.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ComplaintMappingService {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintMappingService.class);

    public Complaint mapFromAi(StructuredComplaintData data, String description, User user) {
        Category category = normalizeCategory(data.getCategory());
        String subCategory = normalizeText(data.getSubCategory(), "General");
        String roomNo = normalizeText(data.getRoomNo(), "UNKNOWN");
        String block = normalizeText(data.getBlock(), "UNKNOWN");
        String roomType = normalizeRoomType(data.getRoomType());
        String preferredTimeSlot = normalizeText(data.getPreferredTimeSlot(), "Any");
        int priorityLevel = normalizePriorityLevel(data.getPriorityLevel());

        String assignedTeam = resolveAssignedTeam(category);

        Complaint complaint = new Complaint();
        complaint.setMessageType(MessageType.GRIEVANCE);
        complaint.setType("GRIEVANCE");
        complaint.setStatus(Status.OPEN);

        complaint.setCategory(category);
        complaint.setSubCategory(subCategory);
        complaint.setBlock(block);
        complaint.setRoomNo(roomNo);
        complaint.setRoomType(roomType);
        complaint.setDescription(description);
        complaint.setRaisedBy(user);

        complaint.setAssignedTo(assignedTeam);
        complaint.setAssignedTeam(assignedTeam);

        complaint.setTimeSlot(preferredTimeSlot);
        complaint.setPreferredTimeSlot(preferredTimeSlot);

        String phoneNumber = user.getContactNumber();
        complaint.setContactNo(phoneNumber);
        complaint.setPhoneNumber(phoneNumber);

        complaint.setStudentName(user.getFullName());

        LocalDate today = LocalDate.now();
        complaint.setComplaintDate(today);
        complaint.setAvailabilityDate(today);

        complaint.setPriorityLevel(priorityLevel);
        complaint.setBuildingCode(block);

        return complaint;
    }

    private Category normalizeCategory(String value) {
        if (value == null) {
            return Category.GENERAL;
        }

        String normalized = value.trim().toUpperCase();
        try {
            return Category.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid category '{}' received from AI. Defaulting to GENERAL.", value);
            return Category.GENERAL;
        }
    }

    private String normalizeRoomType(String value) {
        if (value == null || value.isBlank()) {
            return "Single";
        }

        String normalized = value.trim().toLowerCase();
        if (normalized.equals("single")) {
            return "Single";
        }
        if (normalized.equals("double")) {
            return "Double";
        }

        logger.warn("Invalid roomType '{}' received from AI. Defaulting to Single.", value);
        return "Single";
    }

    private int normalizePriorityLevel(Integer value) {
        if (value == null || value < 1 || value > 10) {
            return 5;
        }
        return value;
    }

    private String normalizeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private String resolveAssignedTeam(Category category) {
        return switch (category) {
            case PLUMBING -> "Plumber Team";
            case ELECTRICAL -> "Electrician Team";
            case RAGGING -> "Warden Team";
            case CARPENTRY -> "Carpenter Team";
            case GENERAL -> "Admin Team";
        };
    }
}
