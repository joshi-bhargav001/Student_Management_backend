package com.example.StdManagement.dto.Request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

    private String name;

    private String username;

    private String email;

    private String password;

    private String confirmPassword;
}
