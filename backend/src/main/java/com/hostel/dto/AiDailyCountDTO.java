package com.hostel.dto;

import java.time.LocalDate;

public class AiDailyCountDTO {

    private LocalDate date;
    private long total;
    private long admin;
    private long user;

    public AiDailyCountDTO() {}

    public AiDailyCountDTO(LocalDate date, long total, long admin, long user) {
        this.date = date;
        this.total = total;
        this.admin = admin;
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getAdmin() {
        return admin;
    }

    public void setAdmin(long admin) {
        this.admin = admin;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }
}
