package com.example.StdManagement.repository;

import com.example.StdManagement.entity.Attendance;
import com.example.StdManagement.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance,Long> {

    boolean existsByStudentIdAndDate(Long studentId, LocalDate date);

    boolean existsByStudentIdAndDateAndIdNot(Long studentId, LocalDate date, Long id);

    List<Attendance> findByDateAndStudentCourseIgnoreCaseAndStudentDivisionIgnoreCase(
            LocalDate date, String course, String division);

    List<Attendance> findByDateAndStudentIdIn(LocalDate date, List<Long> studentIds);

    long countByDateAndStatus(LocalDate date, AttendanceStatus status);

    @Query("""
            select a.date as date,
                   sum(case when a.status = com.example.StdManagement.enums.AttendanceStatus.PRESENT then 1 else 0 end) as present,
                   sum(case when a.status = com.example.StdManagement.enums.AttendanceStatus.ABSENT then 1 else 0 end) as absent,
                   sum(case when a.status = com.example.StdManagement.enums.AttendanceStatus.LEAVE then 1 else 0 end) as leaveCount
            from Attendance a
            where a.date between :startDate and :endDate
            group by a.date
            order by a.date
            """)
    List<AttendanceOverviewCount> findAttendanceOverview(LocalDate startDate, LocalDate endDate);

    interface AttendanceOverviewCount {

        LocalDate getDate();

        long getPresent();

        long getAbsent();

        long getLeaveCount();
    }
}
