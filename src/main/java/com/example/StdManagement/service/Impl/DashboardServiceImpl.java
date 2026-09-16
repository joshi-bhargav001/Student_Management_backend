package com.example.StdManagement.service.Impl;

import com.example.StdManagement.dto.Response.DashboardResponse;
import com.example.StdManagement.enums.AttendanceStatus;
import com.example.StdManagement.repository.AttendanceRepository;
import com.example.StdManagement.repository.CourseRepository;
import com.example.StdManagement.repository.StudentRepository;
import com.example.StdManagement.repository.TeacherRepository;
import com.example.StdManagement.service.DashboardService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int ATTENDANCE_OVERVIEW_DAYS = 7;

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate overviewStartDate = today.minusDays(ATTENDANCE_OVERVIEW_DAYS - 1L);

        long present = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.ABSENT);
        long leave = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.LEAVE);
        long total = present + absent + leave;

        return DashboardResponse.builder()
                .totalStudents(studentRepository.count())
                .totalCourses(courseRepository.count())
                .totalDivisions(studentRepository.countDistinctDivisions())
                .totalTeachers(teacherRepository.count())
                .todayAttendance(DashboardResponse.TodayAttendance.builder()
                        .present(present)
                        .absent(absent)
                        .leave(leave)
                        .total(total)
                        .percentage(calculatePercentage(present, total))
                        .build())
                .attendanceOverview(buildAttendanceOverview(overviewStartDate, today))
                .courseWiseStudents(buildCourseWiseStudents())
                .build();
    }

    private List<DashboardResponse.AttendanceOverview> buildAttendanceOverview(
            LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findAttendanceOverview(startDate, endDate)
                .stream()
                .map(row -> DashboardResponse.AttendanceOverview.builder()
                        .date(row.getDate())
                        .present(row.getPresent())
                        .absent(row.getAbsent())
                        .leave(row.getLeaveCount())
                        .build())
                .toList();
    }

    private List<DashboardResponse.CourseWiseStudents> buildCourseWiseStudents() {
        return studentRepository.countStudentsByCourse()
                .stream()
                .map(row -> DashboardResponse.CourseWiseStudents.builder()
                        .course(row.getCourse())
                        .students(row.getStudents())
                        .build())
                .toList();
    }

    private long calculatePercentage(long present, long total) {
        if (total == 0) {
            return 0;
        }
        return Math.round((present * 100.0) / total);
    }
}
