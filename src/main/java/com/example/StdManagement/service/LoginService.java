package com.example.StdManagement.service;

import com.example.StdManagement.dto.Request.LoginRequest;
import com.example.StdManagement.dto.Response.LoginResponse;

public interface LoginService {

    LoginResponse login(LoginRequest request);
}
