package com.hostel.dto;

public class AdminQuestionRequest {
    private String question;
    private Long userId;

    public AdminQuestionRequest() {}

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
