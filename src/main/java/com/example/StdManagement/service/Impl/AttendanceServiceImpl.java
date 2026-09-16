package com.example.StdManagement.service.Impl;

import com.example.StdManagement.dto.Request.AttendanceRequest;
import com.example.StdManagement.dto.Response.AttendanceResponse;
import com.example.StdManagement.entity.Attendance;
import com.example.StdManagement.entity.Student;
import com.example.StdManagement.exception.DuplicateResourceException;
import com.example.StdManagement.exception.ResourceNotFoundException;
import com.example.StdManagement.repository.AttendanceRepository;
import com.example.StdManagement.repository.StudentRepository;
import com.example.StdManagement.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    @Override
    public List<AttendanceResponse> markAttendance(AttendanceRequest request) {
        Set<AttendanceKey> submittedRecords = new HashSet<>();
        List<Attendance> attendances = request.getAttendances().stream()
                .map(attendanceRequest -> {
                    AttendanceKey key = new AttendanceKey(
                            attendanceRequest.getStudentId(), attendanceRequest.getDate());

                    if (!submittedRecords.add(key)) {
                        throw new DuplicateResourceException(
                                "Duplicate attendance record for student id "
                                        + attendanceRequest.getStudentId() + " on "
                                        + attendanceRequest.getDate());
                    }

                    Student student = studentRepository.findById(attendanceRequest.getStudentId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Student not found with id: " + attendanceRequest.getStudentId()));

                    if (attendanceRepository.existsByStudentIdAndDate(
                            attendanceRequest.getStudentId(), attendanceRequest.getDate())) {
                        throw new DuplicateResourceException(
                                "Attendance is already marked for this student on "
                                        + attendanceRequest.getDate());
                    }

                    return Attendance.builder()
                            .student(student)
                            .date(attendanceRequest.getDate())
                            .status(attendanceRequest.getStatus())
                            .build();
                })
                .toList();

        return attendanceRepository.saveAll(attendances).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public AttendanceResponse updateAttendance(Long id, AttendanceRequest.AttendanceItem request) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + id));

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.getStudentId()));

        if (attendanceRepository.existsByStudentIdAndDateAndIdNot(
                request.getStudentId(), request.getDate(), id)) {
            throw new DuplicateResourceException(
                    "Attendance is already marked for this student on " + request.getDate());
        }

        attendance.setStudent(student);
        attendance.setStatus(request.getStatus());
        attendance.setDate(request.getDate());

        return mapToResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendance(LocalDate date, String course, String division) {
        List<Student> students = studentRepository
                .findByCourseIgnoreCaseAndDivisionIgnoreCase(course, division);

        Map<Long, Attendance> attendanceByStudentId = attendanceRepository
                .findByDateAndStudentIdIn(date, students.stream().map(Student::getId).toList())
                .stream()
                .collect(Collectors.toMap(attendance -> attendance.getStudent().getId(), Function.identity()));

        return students.stream()
                .map(student -> mapToResponse(student, attendanceByStudentId.get(student.getId()), date))
                .toList();
    }

    private AttendanceResponse mapToResponse(Student student, Attendance attendance, LocalDate date) {
        return AttendanceResponse.builder()
                .id(attendance == null ? null : attendance.getId())
                .studentId(student.getId())
                .studentName(student.getName())
                .rollNo(student.getRollNo())
                .date(date)
                .status(attendance == null ? null : attendance.getStatus())
                .build();
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getName())
                .rollNo(attendance.getStudent().getRollNo())
                .date(attendance.getDate())
                .status(attendance.getStatus())
                .build();
    }

    private record AttendanceKey(Long studentId, java.time.LocalDate date) {
    }
}
