package com.example.StdManagement.dto.Response;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalStudents;

    private long totalCourses;

    private long totalDivisions;

    private long totalTeachers;

    private TodayAttendance todayAttendance;

    private List<AttendanceOverview> attendanceOverview;

    private List<CourseWiseStudents> courseWiseStudents;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TodayAttendance {

        private long present;

        private long absent;

        private long leave;

        private long total;

        private long percentage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceOverview {

        private LocalDate date;

        private long present;

        private long absent;

        private long leave;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CourseWiseStudents {

        private String course;

        private long students;
    }
}
