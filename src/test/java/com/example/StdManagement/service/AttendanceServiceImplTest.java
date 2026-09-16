package com.example.StdManagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.StdManagement.dto.Response.AttendanceResponse;
import com.example.StdManagement.entity.Attendance;
import com.example.StdManagement.entity.Student;
import com.example.StdManagement.enums.AttendanceStatus;
import com.example.StdManagement.repository.AttendanceRepository;
import com.example.StdManagement.repository.StudentRepository;
import com.example.StdManagement.service.Impl.AttendanceServiceImpl;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @Test
    void getAttendanceReturnsEveryStudentInCourseDivisionEvenWhenOnlyOneIsMarked() {
        LocalDate date = LocalDate.of(2026, 9, 11);
        Student aarav = student(20L, "Aarav Sharma", "ROLL-001");
        Student vivaan = student(21L, "Vivaan Patel", "ROLL-002");
        Student krishna = student(26L, "Krishna Reddy", "ROLL-007");
        Attendance vivaanAttendance = Attendance.builder()
                .id(1L)
                .student(vivaan)
                .date(date)
                .status(AttendanceStatus.ABSENT)
                .build();

        when(studentRepository.findByCourseIgnoreCaseAndDivisionIgnoreCase("BCA", "A"))
                .thenReturn(List.of(aarav, vivaan, krishna));
        when(attendanceRepository.findByDateAndStudentIdIn(date, List.of(20L, 21L, 26L)))
                .thenReturn(List.of(vivaanAttendance));

        List<AttendanceResponse> attendance = attendanceService.getAttendance(date, "BCA", "A");

        assertThat(attendance).hasSize(3);
        assertThat(attendance)
                .extracting(AttendanceResponse::getStudentId)
                .containsExactly(20L, 21L, 26L);
        assertThat(attendance)
                .filteredOn(response -> response.getStudentId().equals(20L))
                .singleElement()
                .satisfies(response -> {
                    assertThat(response.getId()).isNull();
                    assertThat(response.getStatus()).isNull();
                });
        assertThat(attendance)
                .filteredOn(response -> response.getStudentId().equals(21L))
                .singleElement()
                .satisfies(response -> {
                    assertThat(response.getId()).isEqualTo(1L);
                    assertThat(response.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
                });
        assertThat(attendance)
                .filteredOn(response -> response.getStudentId().equals(26L))
                .singleElement()
                .satisfies(response -> {
                    assertThat(response.getId()).isNull();
                    assertThat(response.getStatus()).isNull();
                });

        verify(studentRepository).findByCourseIgnoreCaseAndDivisionIgnoreCase("BCA", "A");
        verify(attendanceRepository).findByDateAndStudentIdIn(date, List.of(20L, 21L, 26L));
    }

    private Student student(Long id, String name, String rollNo) {
        return Student.builder()
                .id(id)
                .name(name)
                .email(id + "@example.com")
                .course("BCA")
                .division("A")
                .mobile("9876543210")
                .rollNo(rollNo)
                .build();
    }
}
