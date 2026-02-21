package com.hostel.controller;

import com.hostel.dto.AdminQuestionRequest;
import com.hostel.dto.AgentAnswerResponse;
import com.hostel.service.AgentQaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/qa")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminQaController {

    @Autowired
    private AgentQaService agentQaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AgentAnswerResponse> askAdminQuestion(@RequestBody AdminQuestionRequest request) {
        String answer = agentQaService.answerAdminQuestion(request.getQuestion(), request.getUserId());
        return ResponseEntity.ok(new AgentAnswerResponse(answer));
    }
}
