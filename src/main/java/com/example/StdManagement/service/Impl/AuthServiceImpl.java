package com.example.StdManagement.service.Impl;

import com.example.StdManagement.dto.Request.LoginRequest;
import com.example.StdManagement.dto.Request.SignUpRequest;
import com.example.StdManagement.dto.Response.LoginResponse;
import com.example.StdManagement.dto.Response.SignUpResponse;
import com.example.StdManagement.entity.User;
import com.example.StdManagement.enums.Role;
import com.example.StdManagement.exception.DuplicateResourceException;
import com.example.StdManagement.repository.UserRepository;
import com.example.StdManagement.security.JwtService;
import com.example.StdManagement.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

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
                .password(passwordEncoder.encode(request.getPassword()))
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

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (passwordEncoder.upgradeEncoding(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
        }

        String token = jwtService.generateToken((UserDetails) authentication.getPrincipal());

        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .token(token)
                .message("Login successful")
                .role(user.getRole())
                .build();
    }
}
