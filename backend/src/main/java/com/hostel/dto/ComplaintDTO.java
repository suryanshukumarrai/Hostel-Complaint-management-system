package com.hostel.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hostel.entity.Category;
import com.hostel.entity.MessageType;
import com.hostel.entity.PriorityLevel;
import com.hostel.entity.Status;

public class ComplaintDTO {
    private Long id;
    private MessageType messageType;
    private Category category;
    private String subCategory;
    private String specificCategory;
    private String block;
    private String subBlock;
    private String roomType;
    private String roomNo;
    private String buildingCode;
    private PriorityLevel priorityLevel;
    private String contactNo;
    private LocalDate availabilityDate;
    private String timeSlot;
    private String preferredTimeSlot;
    private String description;
    private String assignedTo;
    private String assignedTeam;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime createdTimestamp;
    private UserDTO raisedBy;
    private String imageUrl;
    private String attachmentPath;
    private String phoneNumber;
    private String studentName;
    private LocalDate complaintDate;
    private String type;

    public ComplaintDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    public String getSpecificCategory() { return specificCategory; }
    public void setSpecificCategory(String specificCategory) { this.specificCategory = specificCategory; }
    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }
    public String getSubBlock() { return subBlock; }
    public void setSubBlock(String subBlock) { this.subBlock = subBlock; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public String getBuildingCode() { return buildingCode; }
    public void setBuildingCode(String buildingCode) { this.buildingCode = buildingCode; }
    public PriorityLevel getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(PriorityLevel priorityLevel) { this.priorityLevel = priorityLevel; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public LocalDate getAvailabilityDate() { return availabilityDate; }
    public void setAvailabilityDate(LocalDate availabilityDate) { this.availabilityDate = availabilityDate; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getPreferredTimeSlot() { return preferredTimeSlot; }
    public void setPreferredTimeSlot(String preferredTimeSlot) { this.preferredTimeSlot = preferredTimeSlot; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }
    public UserDTO getRaisedBy() { return raisedBy; }
    public void setRaisedBy(UserDTO raisedBy) { this.raisedBy = raisedBy; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public LocalDate getComplaintDate() { return complaintDate; }
    public void setComplaintDate(LocalDate complaintDate) { this.complaintDate = complaintDate; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
