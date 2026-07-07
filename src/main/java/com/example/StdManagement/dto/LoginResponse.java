package com.example.StdManagement.dto;

import com.example.StdManagement.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {

    private Long id;

    private String name;

    private String username;

    private Role role;
}
