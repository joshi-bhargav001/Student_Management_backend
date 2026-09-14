package com.example.StdManagement.dto.Request;

import com.example.StdManagement.enums.CourseStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequest {

    private String courseName;

    private Integer duration;

    private Integer totalSemester;

    private String department;

    private CourseStatus status;
}
