package com.example.StdManagement.dto.Response;

import com.example.StdManagement.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private Long id;

    private String name;

    private String username;

    private String token;

    private String refreshToken;

    private String message;

    private Role role;
}
