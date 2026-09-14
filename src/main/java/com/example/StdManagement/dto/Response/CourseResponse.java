package com.example.StdManagement.dto.Response;

import com.example.StdManagement.enums.CourseStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseResponse {

    private Long id;

    private String courseName;

    private Integer duration;

    private Integer totalSemester;

    private String department;

    private CourseStatus status;

    private LocalDateTime createAt;
}
