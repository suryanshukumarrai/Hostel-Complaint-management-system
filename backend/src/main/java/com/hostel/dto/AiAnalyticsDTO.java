package com.hostel.dto;

import java.time.LocalDate;

public class AiAnalyticsDTO {

    private long totalQuestions;
    private long totalAdminQuestions;
    private long totalUserQuestions;
    private long successCount;
    private long errorCount;
    private LocalDate firstQuestionDate;
    private LocalDate lastQuestionDate;

    public AiAnalyticsDTO() {}

    public AiAnalyticsDTO(long totalQuestions,
                          long totalAdminQuestions,
                          long totalUserQuestions,
                          long successCount,
                          long errorCount,
                          LocalDate firstQuestionDate,
                          LocalDate lastQuestionDate) {
        this.totalQuestions = totalQuestions;
        this.totalAdminQuestions = totalAdminQuestions;
        this.totalUserQuestions = totalUserQuestions;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.firstQuestionDate = firstQuestionDate;
        this.lastQuestionDate = lastQuestionDate;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public long getTotalAdminQuestions() {
        return totalAdminQuestions;
    }

    public void setTotalAdminQuestions(long totalAdminQuestions) {
        this.totalAdminQuestions = totalAdminQuestions;
    }

    public long getTotalUserQuestions() {
        return totalUserQuestions;
    }

    public void setTotalUserQuestions(long totalUserQuestions) {
        this.totalUserQuestions = totalUserQuestions;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public LocalDate getFirstQuestionDate() {
        return firstQuestionDate;
    }

    public void setFirstQuestionDate(LocalDate firstQuestionDate) {
        this.firstQuestionDate = firstQuestionDate;
    }

    public LocalDate getLastQuestionDate() {
        return lastQuestionDate;
    }

    public void setLastQuestionDate(LocalDate lastQuestionDate) {
        this.lastQuestionDate = lastQuestionDate;
    }
}
