package com.hostel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiStructuredComplaintFields {
    @JsonProperty("category")
    private String category;

    @JsonProperty("sub_category")
    private String subCategory;

    @JsonProperty("specific_category")
    private String specificCategory;

    @JsonProperty("block")
    private String block;

    @JsonProperty("room_no")
    private String roomNo;

    @JsonProperty("priority_level")
    private String priorityLevel;

    @JsonProperty("message_type")
    private String messageType;

    @JsonProperty("room_type")
    private String roomType;

    @JsonProperty("building_code")
    private String buildingCode;

    @JsonProperty("sub_block")
    private String subBlock;

    public AiStructuredComplaintFields() {}

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
    public String getSpecificCategory() { return specificCategory; }
    public void setSpecificCategory(String specificCategory) { this.specificCategory = specificCategory; }
    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    public String getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public String getBuildingCode() { return buildingCode; }
    public void setBuildingCode(String buildingCode) { this.buildingCode = buildingCode; }
    public String getSubBlock() { return subBlock; }
    public void setSubBlock(String subBlock) { this.subBlock = subBlock; }
}
