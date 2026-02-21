package com.hostel.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "qa_history")
public class QaHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_admin", nullable = false)
    private boolean admin;

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(nullable = false, length = 4000)
    private String answer;

    @Column(name = "asked_at", nullable = false)
    private LocalDateTime askedAt;

    public QaHistory() {}

    public QaHistory(Long userId, boolean admin, String question, String answer, LocalDateTime askedAt) {
        this.userId = userId;
        this.admin = admin;
        this.question = question;
        this.answer = answer;
        this.askedAt = askedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
