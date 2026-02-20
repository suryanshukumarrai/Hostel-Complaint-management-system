package com.hostel.dto;

public class AgentQuestionRequest {
    private String question;
    private Long userId;

    public AgentQuestionRequest() {}

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
