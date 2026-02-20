package com.hostel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StructuredComplaintData {
    private String category;
    private String subCategory;
    private String roomNo;
    private String block;
    private String roomType;
    private Integer priorityLevel;
    private String assignedTeam;
    private String preferredTimeSlot;

    public StructuredComplaintData() {}

    public StructuredComplaintData(String category, String subCategory, String roomNo, String block,
                                   String roomType, Integer priorityLevel, String assignedTeam,
                                   String preferredTimeSlot) {
        this.category = category;
        this.subCategory = subCategory;
        this.roomNo = roomNo;
        this.block = block;
        this.roomType = roomType;
        this.priorityLevel = priorityLevel;
        this.assignedTeam = assignedTeam;
        this.preferredTimeSlot = preferredTimeSlot;
    }

    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("subCategory")
    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    @JsonProperty("roomNo")
    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    @JsonProperty("block")
    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    @JsonProperty("roomType")
    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    @JsonProperty("priorityLevel")
    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    @JsonProperty("assignedTeam")
    public String getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(String assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    @JsonProperty("preferredTimeSlot")
    public String getPreferredTimeSlot() {
        return preferredTimeSlot;
    }

    public void setPreferredTimeSlot(String preferredTimeSlot) {
        this.preferredTimeSlot = preferredTimeSlot;
    }
}
