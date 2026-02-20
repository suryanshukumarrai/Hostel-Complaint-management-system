package com.hostel.controller;

import com.hostel.dto.QaHistoryDTO;
import com.hostel.dto.AiAnalyticsDTO;
import com.hostel.dto.AiDailyCountDTO;
import com.hostel.entity.QaHistory;
import com.hostel.repository.QaHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/qa/history")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class QaHistoryController {

    @Autowired
    private QaHistoryRepository qaHistoryRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<List<QaHistoryDTO>> getHistoryForUser(@PathVariable Long userId) {
        List<QaHistory> history = qaHistoryRepository.findTop20ByUserIdOrderByAskedAtDesc(userId);
        List<QaHistoryDTO> dtoList = history.stream()
                .map(h -> new QaHistoryDTO(h.getId(), h.isAdmin(), h.getQuestion(), h.getAnswer(), h.getAskedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/analytics/global")
    public ResponseEntity<AiAnalyticsDTO> getGlobalAnalytics() {
        List<QaHistory> all = qaHistoryRepository.findAll();
        if (all.isEmpty()) {
            return ResponseEntity.ok(new AiAnalyticsDTO(0, 0, 0, 0, 0, null, null));
        }

        long total = all.size();
        long totalAdmin = all.stream().filter(QaHistory::isAdmin).count();
        long totalUser = total - totalAdmin;
        long errorCount = all.stream()
                .filter(h -> isErrorAnswer(h.getAnswer()))
                .count();
        long successCount = total - errorCount;

        QaHistory first = all.stream().min(Comparator.comparing(QaHistory::getAskedAt)).orElse(all.get(0));
        QaHistory last = all.stream().max(Comparator.comparing(QaHistory::getAskedAt)).orElse(all.get(all.size() - 1));

        AiAnalyticsDTO dto = new AiAnalyticsDTO(
            total,
            totalAdmin,
            totalUser,
            successCount,
            errorCount,
            first.getAskedAt().toLocalDate(),
            last.getAskedAt().toLocalDate()
        );
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/analytics/user/{userId}")
    public ResponseEntity<AiAnalyticsDTO> getUserAnalytics(@PathVariable Long userId) {
        List<QaHistory> history = qaHistoryRepository.findTop20ByUserIdOrderByAskedAtDesc(userId);
        if (history.isEmpty()) {
            return ResponseEntity.ok(new AiAnalyticsDTO(0, 0, 0, 0, 0, null, null));
        }

        long total = history.size();
        long totalAdmin = history.stream().filter(QaHistory::isAdmin).count();
        long totalUser = total - totalAdmin;
        long errorCount = history.stream()
                .filter(h -> isErrorAnswer(h.getAnswer()))
                .count();
        long successCount = total - errorCount;

        QaHistory first = history.get(history.size() - 1);
        QaHistory last = history.get(0);

        AiAnalyticsDTO dto = new AiAnalyticsDTO(
            total,
            totalAdmin,
            totalUser,
            successCount,
            errorCount,
            first.getAskedAt().toLocalDate(),
            last.getAskedAt().toLocalDate()
        );
        return ResponseEntity.ok(dto);
    }

        private boolean isErrorAnswer(String answer) {
        if (answer == null) return false;
        String a = answer.toLowerCase();
        return a.startsWith("error calling llm api")
            || a.contains("no response from llm api")
            || a.contains("unexpected response format from llm api")
            || a.contains("llm configuration is missing");
        }

        @GetMapping("/analytics/global/daily")
        public ResponseEntity<List<AiDailyCountDTO>> getGlobalDailyCounts(@RequestParam(defaultValue = "7") int days) {
        if (days <= 0) days = 7;
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1);
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = today.atTime(LocalTime.MAX);

        List<QaHistory> all = qaHistoryRepository.findAll().stream()
            .filter(h -> !h.getAskedAt().isBefore(from) && !h.getAskedAt().isAfter(to))
            .collect(Collectors.toList());

        Map<LocalDate, List<QaHistory>> byDate = all.stream()
            .collect(Collectors.groupingBy(h -> h.getAskedAt().toLocalDate()));

        List<AiDailyCountDTO> result = start.datesUntil(today.plusDays(1))
            .map(date -> {
                List<QaHistory> list = byDate.getOrDefault(date, List.of());
                long total = list.size();
                long admin = list.stream().filter(QaHistory::isAdmin).count();
                long user = total - admin;
                return new AiDailyCountDTO(date, total, admin, user);
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
        }

        @GetMapping("/analytics/user/{userId}/daily")
        public ResponseEntity<List<AiDailyCountDTO>> getUserDailyCounts(@PathVariable Long userId,
                                        @RequestParam(defaultValue = "7") int days) {
        if (days <= 0) days = 7;
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(days - 1);
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = today.atTime(LocalTime.MAX);

        List<QaHistory> history = qaHistoryRepository.findTop20ByUserIdOrderByAskedAtDesc(userId).stream()
            .filter(h -> !h.getAskedAt().isBefore(from) && !h.getAskedAt().isAfter(to))
            .collect(Collectors.toList());

        Map<LocalDate, List<QaHistory>> byDate = history.stream()
            .collect(Collectors.groupingBy(h -> h.getAskedAt().toLocalDate()));

        List<AiDailyCountDTO> result = start.datesUntil(today.plusDays(1))
            .map(date -> {
                List<QaHistory> list = byDate.getOrDefault(date, List.of());
                long total = list.size();
                long admin = list.stream().filter(QaHistory::isAdmin).count();
                long user = total - admin;
                return new AiDailyCountDTO(date, total, admin, user);
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
        }
}
