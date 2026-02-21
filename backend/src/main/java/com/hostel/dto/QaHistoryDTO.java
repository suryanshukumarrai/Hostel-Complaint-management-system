package com.hostel.dto;

import java.time.LocalDateTime;

public class QaHistoryDTO {

    private Long id;
    private boolean admin;
    private String question;
    private String answer;
    private LocalDateTime askedAt;

    public QaHistoryDTO() {}

    public QaHistoryDTO(Long id, boolean admin, String question, String answer, LocalDateTime askedAt) {
        this.id = id;
        this.admin = admin;
        this.question = question;
        this.answer = answer;
        this.askedAt = askedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(LocalDateTime askedAt) {
        this.askedAt = askedAt;
    }
}
