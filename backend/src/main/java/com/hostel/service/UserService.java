package com.hostel.service;

import com.hostel.dto.UserDTO;
import com.hostel.entity.User;
import com.hostel.exception.ResourceNotFoundException;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getClients() {
        return userRepository.findAll().stream()
                .filter(u -> "CLIENT".equalsIgnoreCase(u.getRole()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO createUser(String name, String role) {
        User user = new User();
        user.setFullName(name);
        user.setRole(role);
        User saved = userRepository.save(user);
        return convertToDTO(saved);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getFullName());
        dto.setRole(user.getRole());
        return dto;
    }
}
