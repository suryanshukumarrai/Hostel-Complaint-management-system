package com.hostel.dto;

public class AiComplaintResponse {
    private Long id;
    private String category;
    private String subCategory;
    private String roomNo;
    private String priority;
    private Integer priorityLevel;
    private String status;
    private String description;
    private String message;

    public AiComplaintResponse() {}

    public AiComplaintResponse(Long id, String category, String subCategory, String roomNo, Integer priorityLevel, String status, String description) {
        this.id = id;
        this.category = category;
        this.subCategory = subCategory;
        this.roomNo = roomNo;
        this.priorityLevel = priorityLevel;
        this.priority = priorityLevel != null ? String.valueOf(priorityLevel) : null;
        this.status = status;
        this.description = description;
        this.message = "Complaint generated successfully";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
        this.priority = priorityLevel != null ? String.valueOf(priorityLevel) : this.priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
