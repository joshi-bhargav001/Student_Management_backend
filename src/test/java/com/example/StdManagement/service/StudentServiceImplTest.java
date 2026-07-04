package com.example.StdManagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.StdManagement.dto.StudentRequest;
import com.example.StdManagement.dto.StudentResponse;
import com.example.StdManagement.entity.Student;
import com.example.StdManagement.exception.DuplicateResourceException;
import com.example.StdManagement.exception.ResourceNotFoundException;
import com.example.StdManagement.repository.StudentRepository;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void createStudentRejectsDuplicateEmail() {
        StudentRequest request = request("R001", "same@example.com");
        when(studentRepository.existsByEmail("same@example.com")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email already exists");

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void createStudentRejectsDuplicateRollNo() {
        StudentRequest request = request("R001", "student@example.com");
        when(studentRepository.existsByRollNo("R001")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("roll number already exists");

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void updateStudentRejectsDuplicateRollNoUsedByAnotherStudent() {
        StudentRequest request = request("R002", "student@example.com");
        Student student = student(1L, "R001", "old@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByRollNoAndIdNot("R002", 1L)).thenReturn(true);

        assertThatThrownBy(() -> studentService.updateStudent(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("roll number already exists");

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void createStudentReturnsRollNoInResponse() {
        StudentRequest request = request("R001", "student@example.com");
        Student saved = student(1L, "R001", "student@example.com");
        when(studentRepository.save(any(Student.class))).thenReturn(saved);

        StudentResponse response = studentService.createStudent(request);

        assertThat(response.getRollNo()).isEqualTo("R001");
    }

    @Test
    void getAllStudentsReturnsMappedResponses() {
        when(studentRepository.findAll()).thenReturn(List.of(
                student(1L, "R001", "first@example.com"),
                student(2L, "R002", "second@example.com")));

        List<StudentResponse> students = studentService.getAllStudents();

        assertThat(students).hasSize(2);
        assertThat(students)
                .extracting(StudentResponse::getRollNo)
                .containsExactly("R001", "R002");
    }

    @Test
    void getStudentByIdReturnsMappedResponse() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(1L, "R001", "student@example.com")));

        StudentResponse response = studentService.getStudentById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRollNo()).isEqualTo("R001");
        assertThat(response.getEmail()).isEqualTo("student@example.com");
    }

    @Test
    void getStudentByIdThrowsWhenStudentDoesNotExist() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student not found with id: 99");
    }

    @Test
    void updateStudentSavesChangedFields() {
        StudentRequest request = request("R002", "updated@example.com");
        Student student = student(1L, "R001", "old@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        StudentResponse response = studentService.updateStudent(1L, request);

        assertThat(response.getRollNo()).isEqualTo("R002");
        assertThat(response.getEmail()).isEqualTo("updated@example.com");
        assertThat(response.getName()).isEqualTo("Student Name");
        verify(studentRepository).save(student);
    }

    @Test
    void updateStudentRejectsDuplicateEmailUsedByAnotherStudent() {
        StudentRequest request = request("R001", "duplicate@example.com");
        Student student = student(1L, "R001", "old@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmailAndIdNot("duplicate@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> studentService.updateStudent(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email already exists");

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void deleteStudentDeletesExistingStudent() {
        Student student = student(1L, "R001", "student@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.deleteStudent(1L);

        verify(studentRepository).delete(student);
    }

    @Test
    void deleteStudentThrowsWhenStudentDoesNotExist() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.deleteStudent(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student not found with id: 99");
    }

    private StudentRequest request(String rollNo, String email) {
        return StudentRequest.builder()
                .rollNo(rollNo)
                .name("Student Name")
                .email(email)
                .course("Computer Science")
                .mobile("9876543210")
                .build();
    }

    private Student student(Long id, String rollNo, String email) {
        return Student.builder()
                .id(id)
                .rollNo(rollNo)
                .name("Student Name")
                .email(email)
                .course("Computer Science")
                .mobile("9876543210")
                .build();
    }
}
