package com.example.StdManagement.service;

import com.example.StdManagement.dto.Request.LoginRequest;
import com.example.StdManagement.dto.Request.SignUpRequest;
import com.example.StdManagement.dto.Response.LoginResponse;
import com.example.StdManagement.dto.Response.SignUpResponse;

public interface AuthService {

    SignUpResponse insertRegister(SignUpRequest request);
}
