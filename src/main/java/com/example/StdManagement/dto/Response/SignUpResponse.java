package com.example.StdManagement.dto.Response;

import com.example.StdManagement.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpResponse {

    private Long id;

    private String name;

    private String username;

    private String email;

    private Role role;
}
