package com.hostel.repository;

import com.hostel.entity.QaHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface QaHistoryRepository extends JpaRepository<QaHistory, Long> {

    List<QaHistory> findTop20ByUserIdOrderByAskedAtDesc(Long userId);

    long countByUserId(Long userId);

    long countByAdminTrue();

    long countByAskedAtBetween(LocalDateTime start, LocalDateTime end);
}
