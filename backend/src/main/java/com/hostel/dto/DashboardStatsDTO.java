package com.hostel.dto;

import java.util.Map;

public class DashboardStatsDTO {
    private Long total;
    private Long open;
    private Long inProgress;
    private Long resolved;
    private Map<String, Long> categoryCounts;

    public DashboardStatsDTO() {}

    public DashboardStatsDTO(Long total, Long open, Long inProgress, Long resolved, Map<String, Long> categoryCounts) {
        this.total = total;
        this.open = open;
        this.inProgress = inProgress;
        this.resolved = resolved;
        this.categoryCounts = categoryCounts;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getOpen() {
        return open;
    }

    public void setOpen(Long open) {
        this.open = open;
    }

    public Long getInProgress() {
        return inProgress;
    }

    public void setInProgress(Long inProgress) {
        this.inProgress = inProgress;
    }

    public Long getResolved() {
        return resolved;
    }

    public void setResolved(Long resolved) {
        this.resolved = resolved;
    }

    public Map<String, Long> getCategoryCounts() {
        return categoryCounts;
    }

    public void setCategoryCounts(Map<String, Long> categoryCounts) {
        this.categoryCounts = categoryCounts;
    }
}
