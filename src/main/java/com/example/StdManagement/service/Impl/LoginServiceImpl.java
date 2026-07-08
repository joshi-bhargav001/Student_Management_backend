package com.example.StdManagement.service.Impl;

import com.example.StdManagement.dto.Request.LoginRequest;
import com.example.StdManagement.dto.Response.LoginResponse;
import com.example.StdManagement.entity.User;
import com.example.StdManagement.repository.LoginRepository;
import com.example.StdManagement.service.LoginService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginServiceImpl implements LoginService {

    private final LoginRepository loginRepository;

    public LoginResponse login(LoginRequest request) {
        User user = loginRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new RuntimeException("Invalid username"));

        if(!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
