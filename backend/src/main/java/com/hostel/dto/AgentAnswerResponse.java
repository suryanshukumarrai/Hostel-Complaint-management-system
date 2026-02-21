package com.hostel.dto;

public class AgentAnswerResponse {
    private String answer;

    public AgentAnswerResponse() {}

    public AgentAnswerResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
