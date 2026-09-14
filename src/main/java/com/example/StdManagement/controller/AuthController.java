package com.example.StdManagement.controller;

import com.example.StdManagement.dto.Request.LoginRequest;
import com.example.StdManagement.dto.Request.RefreshRequest;
import com.example.StdManagement.dto.Request.SendOtpRequest;
import com.example.StdManagement.dto.Request.VerifyOtpRequest;
import com.example.StdManagement.dto.Request.SignUpRequest;
import com.example.StdManagement.dto.Response.LoginResponse;
import com.example.StdManagement.dto.Response.SendOtpResponse;
import com.example.StdManagement.dto.Response.VerifyOtpResponse;
import com.example.StdManagement.dto.Response.SignUpResponse;
import jakarta.validation.Valid;
import com.example.StdManagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<SignUpResponse> insertRegister(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.insertRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<SendOtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        return ResponseEntity.ok(authService.sendOtp(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }
}
