package com.example.StdManagement.service;

import com.example.StdManagement.dto.Request.LoginRequest;
import com.example.StdManagement.dto.Request.RefreshRequest;
import com.example.StdManagement.dto.Request.SendOtpRequest;
import com.example.StdManagement.dto.Request.VerifyOtpRequest;
import com.example.StdManagement.dto.Request.SignUpRequest;
import com.example.StdManagement.dto.Response.LoginResponse;
import com.example.StdManagement.dto.Response.SendOtpResponse;
import com.example.StdManagement.dto.Response.VerifyOtpResponse;
import com.example.StdManagement.dto.Response.SignUpResponse;

public interface AuthService {

    SignUpResponse insertRegister(SignUpRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshRequest request);

    SendOtpResponse sendOtp(SendOtpRequest request);

    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);
}
