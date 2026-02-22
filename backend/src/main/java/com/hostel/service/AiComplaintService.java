package com.hostel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostel.dto.AiStructuredComplaintFields;
import com.hostel.dto.ComplaintDTO;
import com.hostel.dto.UserDTO;
import com.hostel.entity.Category;
import com.hostel.entity.Complaint;
import com.hostel.entity.MessageType;
import com.hostel.entity.PriorityLevel;
import com.hostel.entity.Status;
import com.hostel.entity.User;
import com.hostel.exception.ResourceNotFoundException;
import com.hostel.repository.ComplaintRepository;
import com.hostel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Locale;

@Service
public class AiComplaintService {

    private static final Logger logger = LoggerFactory.getLogger(AiComplaintService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private RagLlmClient ragLlmClient;

    @Autowired
    private ChromaClient chromaClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ComplaintDTO generateComplaint(String description, String username) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String systemPrompt = "You are an assistant that converts hostel complaint descriptions into JSON. " +
                "Return ONLY valid JSON with these keys and no extra text: " +
                "category, sub_category, specific_category, block, room_no, priority_level, message_type, " +
                "room_type, building_code, sub_block. " +
                "Use UPPERCASE for category and message_type. " +
                "Allowed category values: CARPENTRY, ELECTRICAL, PLUMBING, RAGGING. " +
                "Allowed message_type values: GRIEVANCE, ASSISTANCE, ENQUIRY, FEEDBACK, POSITIVE_FEEDBACK. " +
                "Allowed priority_level values: LOW, MEDIUM, HIGH, CRITICAL. " +
                "Use null if a field cannot be inferred.";

        String response = ragLlmClient.generateAnswer(systemPrompt, description, "");
        AiStructuredComplaintFields fields = parseStructuredFields(response);

        Category category = parseCategory(fields.getCategory());
        MessageType messageType = parseMessageType(fields.getMessageType());
        PriorityLevel priorityLevel = parsePriorityLevel(fields.getPriorityLevel());
        if (category == null || messageType == null || priorityLevel == null) {
            throw new IllegalArgumentException("AI did not return required fields");
        }

        Complaint complaint = new Complaint();
        complaint.setDescription(description.trim());
        complaint.setCategory(category);
        complaint.setSubCategory(cleanValue(fields.getSubCategory()));
        complaint.setSpecificCategory(cleanValue(fields.getSpecificCategory()));
        complaint.setBlock(cleanValue(fields.getBlock()));
        complaint.setSubBlock(cleanValue(fields.getSubBlock()));
        complaint.setRoomNo(cleanValue(fields.getRoomNo()));
        complaint.setRoomType(cleanValue(fields.getRoomType()));
        complaint.setBuildingCode(cleanValue(fields.getBuildingCode()));
        complaint.setPriorityLevel(priorityLevel);
        complaint.setMessageType(messageType);
        complaint.setRaisedBy(user);
        complaint.setStatus(Status.OPEN);
        complaint.setAssignedTo(null);
        complaint.setAssignedTeam(resolveAssignedTeam(category));
        complaint.setContactNo(user.getContactNumber());
        complaint.setPhoneNumber(user.getContactNumber());
        complaint.setStudentName(user.getFullName());
        complaint.setComplaintDate(LocalDate.now());
        complaint.setType("AI_GENERATED");
        complaint.setImageUrl(null);
        complaint.setAttachmentPath(null);
        complaint.setAvailabilityDate(null);
        complaint.setTimeSlot(null);
        complaint.setPreferredTimeSlot(null);

        Complaint saved = complaintRepository.save(complaint);
        chromaClient.upsertComplaint(saved);

        return toDTO(saved);
    }

    private AiStructuredComplaintFields parseStructuredFields(String response) {
        if (response == null || response.isBlank()) {
            throw new IllegalArgumentException("AI response was empty");
        }
        logger.info("Raw AI response: {}", response);
        String json = extractJson(response);
        logger.info("Extracted JSON: {}", json);
        try {
            return objectMapper.readValue(json, AiStructuredComplaintFields.class);
        } catch (Exception ex) {
            logger.error("Failed to parse AI JSON response: {}", ex.getMessage(), ex);
            throw new IllegalArgumentException("AI response JSON parse failed: " + ex.getMessage());
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalArgumentException("AI response did not contain JSON");
        }
        return response.substring(start, end + 1).trim();
    }

    private Category parseCategory(String raw) {
        String normalized = normalizeEnumValue(raw);
        if (normalized == null) return null;
        try {
            return Category.valueOf(normalized);
        } catch (Exception ex) {
            return null;
        }
    }

    private MessageType parseMessageType(String raw) {
        String normalized = normalizeEnumValue(raw);
        if (normalized == null) return null;
        try {
            return MessageType.valueOf(normalized);
        } catch (Exception ex) {
            return null;
        }
    }

    private PriorityLevel parsePriorityLevel(String raw) {
        String normalized = normalizeEnumValue(raw);
        if (normalized == null) return null;
        try {
            return PriorityLevel.valueOf(normalized);
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizeEnumValue(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) return null;
        String normalized = trimmed.toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return normalized;
    }

    private String cleanValue(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) return null;
        return trimmed;
    }

    private String resolveAssignedTeam(Category category) {
        if (category == null) return null;
        return switch (category) {
            case CARPENTRY -> "Ram";
            case RAGGING -> "Shyam";
            case ELECTRICAL -> "Electric Team";
            case PLUMBING -> "Plumber Team";
        };
    }

    private ComplaintDTO toDTO(Complaint c) {
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
        dto.setBuildingCode(c.getBuildingCode());
        dto.setPriorityLevel(c.getPriorityLevel());
        dto.setContactNo(c.getContactNo());
        dto.setAvailabilityDate(c.getAvailabilityDate());
        dto.setTimeSlot(c.getTimeSlot());
        dto.setPreferredTimeSlot(c.getPreferredTimeSlot());
        dto.setDescription(c.getDescription());
        dto.setAssignedTo(c.getAssignedTo());
        dto.setAssignedTeam(c.getAssignedTeam());
        dto.setStatus(c.getStatus());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setCreatedTimestamp(c.getCreatedTimestamp());
        dto.setImageUrl(c.getImageUrl());
        dto.setAttachmentPath(c.getAttachmentPath());
        dto.setPhoneNumber(c.getPhoneNumber());
        dto.setStudentName(c.getStudentName());
        dto.setComplaintDate(c.getComplaintDate());
        dto.setType(c.getType());

        User u = c.getRaisedBy();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(u.getId());
        userDTO.setName(u.getFullName());
        userDTO.setRole(u.getRole());
        dto.setRaisedBy(userDTO);

        return dto;
    }
}
