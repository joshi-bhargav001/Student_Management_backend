package com.example.StdManagement.repository;

import com.example.StdManagement.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance,Long> {

    boolean existsByStudentIdAndDate(Long studentId, LocalDate date);

    boolean existsByStudentIdAndDateAndIdNot(Long studentId, LocalDate date, Long id);

    List<Attendance> findByDateAndStudentCourseIgnoreCaseAndStudentDivisionIgnoreCase(
            LocalDate date, String course, String division);

    List<Attendance> findByDateAndStudentIdIn(LocalDate date, List<Long> studentIds);
}
