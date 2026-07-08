package com.example.StdManagement.service.Impl;

import com.example.StdManagement.dto.Request.SignUpRequest;
import com.example.StdManagement.dto.Response.SignUpResponse;
import com.example.StdManagement.entity.User;
import com.example.StdManagement.enums.Role;
import com.example.StdManagement.exception.DuplicateResourceException;
import com.example.StdManagement.repository.UserRepository;
import com.example.StdManagement.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{

    public final UserRepository userRepository;

    @Override
    public SignUpResponse insertRegister(SignUpRequest request) {

        if(userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exist");
        }
        if(!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password do not match");
        }
        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(Role.USER)
                .build();
        User saveUser = userRepository.save(user);

        return SignUpResponse.builder()
                .id(saveUser.getId())
                .name(saveUser.getName())
                .username(saveUser.getUsername())
                .email(saveUser.getEmail())
                .role(saveUser.getRole())
                .build();
    }


}
