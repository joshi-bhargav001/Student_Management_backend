package com.example.StdManagement.dto.Response;

import com.example.StdManagement.enums.AttendanceStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceResponse {

    private Long id;

    private Long studentId;

    private String studentName;

    private String rollNo;

    private LocalDate date;

    private AttendanceStatus status;
}
