package com.hostel.dto;

import com.hostel.entity.Status;

public class UpdateStatusRequest {
    private Status status;

    public UpdateStatusRequest() {}

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
