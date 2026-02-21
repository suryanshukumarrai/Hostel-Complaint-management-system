package com.hostel.controller;

import com.hostel.dto.UserDTO;
import com.hostel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/clients")
    public ResponseEntity<List<UserDTO>> getClients() {
        return ResponseEntity.ok(userService.getClients());
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String role = request.get("role");
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(name, role));
    }
}
