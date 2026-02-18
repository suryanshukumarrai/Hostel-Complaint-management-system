package com.hostel.service;

import com.hostel.dto.DashboardStatsDTO;
import com.hostel.entity.Category;
import com.hostel.entity.Status;
import com.hostel.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private ComplaintRepository complaintRepository;

    public DashboardStatsDTO getDashboardStats() {
        // Get total count
        Long total = complaintRepository.count();
        
        // Get counts by status
        Long open = complaintRepository.countByStatus(Status.OPEN);
        Long inProgress = complaintRepository.countByStatus(Status.IN_PROGRESS);
        Long resolved = complaintRepository.countByStatus(Status.RESOLVED);
        
        // Get category counts
        List<Object[]> categoryResults = complaintRepository.countByCategory();
        Map<String, Long> categoryCounts = new HashMap<>();
        
        for (Object[] result : categoryResults) {
            Category category = (Category) result[0];
            Long count = (Long) result[1];
            categoryCounts.put(category.toString(), count);
        }
        
        return new DashboardStatsDTO(total, open, inProgress, resolved, categoryCounts);
    }
}
