package com.hostel.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hostel.entity.Category;
import com.hostel.entity.MessageType;
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
    private String contactNo;
    private LocalDate availabilityDate;
    private String timeSlot;
    private String description;
    private String assignedTo;
    private Status status;
    private LocalDateTime createdAt;
    private UserDTO raisedBy;
    private String imageUrl;

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
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public LocalDate getAvailabilityDate() { return availabilityDate; }
    public void setAvailabilityDate(LocalDate availabilityDate) { this.availabilityDate = availabilityDate; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public UserDTO getRaisedBy() { return raisedBy; }
    public void setRaisedBy(UserDTO raisedBy) { this.raisedBy = raisedBy; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
