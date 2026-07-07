package com.example.StdManagement.service;

import com.example.StdManagement.dto.LoginRequest;
import com.example.StdManagement.dto.LoginResponse;

public interface LoginService {

    LoginResponse login(LoginRequest request);
}
