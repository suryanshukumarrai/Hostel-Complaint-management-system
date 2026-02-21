package com.hostel.controller;

import com.hostel.dto.AgentAnswerResponse;
import com.hostel.dto.AgentQuestionRequest;
import com.hostel.service.AgentQaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients/qa")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AgentQaController {

    @Autowired
    private AgentQaService agentQaService;

    @PostMapping
    public ResponseEntity<AgentAnswerResponse> askQuestion(@RequestBody AgentQuestionRequest request) {
        String answer = agentQaService.answerQuestion(request.getQuestion(), request.getUserId());
        return ResponseEntity.ok(new AgentAnswerResponse(answer));
    }
}
