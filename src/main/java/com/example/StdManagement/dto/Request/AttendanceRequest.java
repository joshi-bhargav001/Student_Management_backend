package com.example.StdManagement.dto.Request;

import com.example.StdManagement.enums.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRequest {

    @NotEmpty(message = "At least one attendance record is required")
    private List<@Valid AttendanceItem> attendances;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceItem {

        @NotNull(message = "Student id is required")
        private Long studentId;

        @NotNull(message = "Attendance status is required")
        private AttendanceStatus status;

        @NotNull(message = "Attendance date is required")
        private LocalDate date;
    }
}
