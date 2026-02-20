package com.hostel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hostel.dto.DashboardStatsDTO;
import com.hostel.service.DashboardService;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(Authentication authentication) {
        logger.info("=== Get Dashboard Stats ===");
        logger.info("User: {}", authentication != null ? authentication.getName() : "Unknown");
        try {
            DashboardStatsDTO stats = dashboardService.getDashboardStats();
            logger.info("Dashboard stats retrieved successfully");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error retrieving dashboard stats: {}", e.getMessage(), e);
            throw e;
        }
    }
}
