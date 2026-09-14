package com.example.StdManagement.dto.Response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherResponse {

    private Long id;
    private String name;
    private String email;
    private String number;
    private String address;
    private String gender;
    private String subject;
    private int experience;
    private double salary;
}
