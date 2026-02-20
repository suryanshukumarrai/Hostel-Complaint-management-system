package com.hostel.dto;

public class AiComplaintRequest {
    private String description;

    public AiComplaintRequest() {}

    public AiComplaintRequest(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
