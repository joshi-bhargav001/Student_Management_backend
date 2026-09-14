package com.example.StdManagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.StdManagement.dto.Request.StudentRequest;
import com.example.StdManagement.dto.Response.StudentResponse;
import com.example.StdManagement.entity.Student;
import com.example.StdManagement.exception.DuplicateResourceException;
import com.example.StdManagement.exception.ResourceNotFoundException;
import com.example.StdManagement.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

import com.example.StdManagement.service.Impl.StudentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void createStudentRejectsDuplicateEmail() {
        StudentRequest request = request("same@example.com");
        when(studentRepository.existsByEmail("same@example.com")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email already exists");

        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void createStudentReturnsRollNoInResponse() {
        StudentRequest request = request("student@example.com");
        when(studentRepository.findAll()).thenReturn(List.of(
                student(1L, "R001", "existing1@example.com"),
                student(2L, "R002", "existing2@example.com")));
        Student saved = student(3L, "R003", "student@example.com");
        when(studentRepository.save(any(Student.class))).thenReturn(saved);

        StudentResponse response = studentService.createStudent(request);

        assertThat(response.getRollNo()).isEqualTo("R003");
    }

    @Test
    void createStudentSkipsExistingGeneratedRollNumbers() {
        StudentRequest request = request("student@example.com");
        when(studentRepository.findAll()).thenReturn(List.of(
                student(1L, "R001", "existing1@example.com"),
                student(2L, "R002", "existing2@example.com")));
        when(studentRepository.existsByRollNo("R003")).thenReturn(true);
        when(studentRepository.existsByRollNo("R004")).thenReturn(false);
        Student saved = student(3L, "R004", "student@example.com");
        when(studentRepository.save(any(Student.class))).thenReturn(saved);

        StudentResponse response = studentService.createStudent(request);

        assertThat(response.getRollNo()).isEqualTo("R004");
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
        StudentRequest request = request("updated@example.com");
        Student student = student(1L, "R001", "old@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        StudentResponse response = studentService.updateStudent(1L, request);

        assertThat(response.getRollNo()).isEqualTo("R001");
        assertThat(response.getEmail()).isEqualTo("updated@example.com");
        assertThat(response.getName()).isEqualTo("Student Name");
        verify(studentRepository).save(student);
    }

    @Test
    void updateStudentRejectsDuplicateEmailUsedByAnotherStudent() {
        StudentRequest request = request("duplicate@example.com");
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

    @Test
    void searchStudentMatchesCourse() {
        Student student = student(1L, "R001", "student@example.com");
        when(studentRepository.findByNameContainingIgnoreCaseOrCourseContainingIgnoreCase("Computer Science", "Computer Science"))
                .thenReturn(List.of(student));

        List<Student> results = studentService.searchStudent("Computer Science");

        assertThat(results).containsExactly(student);
        verify(studentRepository).findByNameContainingIgnoreCaseOrCourseContainingIgnoreCase("Computer Science", "Computer Science");
    }

    @Test
    void searchStudentTrimsKeywordBeforeQuerying() {
        Student student = student(1L, "R001", "student@example.com");
        when(studentRepository.findByNameContainingIgnoreCaseOrCourseContainingIgnoreCase("Computer Science", "Computer Science"))
                .thenReturn(List.of(student));

        List<Student> results = studentService.searchStudent("  Computer Science  ");

        assertThat(results).containsExactly(student);
        verify(studentRepository).findByNameContainingIgnoreCaseOrCourseContainingIgnoreCase("Computer Science", "Computer Science");
    }

    @Test
    void uploadPhotoUpdatesStudentPhotoAndReturnsIt() throws Exception {
        Student student = student(1L, "R001", "student@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.png",
                "image/png",
                "fake-image-content".getBytes());

        StudentResponse response = studentService.uploadPhoto(1L, file);

        assertThat(response.getPhoto()).isNotBlank();
        assertThat(response.getPhoto()).contains("uploads");
        verify(studentRepository).save(student);
    }

    @Test
    void uploadPhotoRejectsMissingFile() {
        Student student = student(1L, "R001", "student@example.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> studentService.uploadPhoto(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File is required");
    }

    private StudentRequest request(String email) {
        return StudentRequest.builder()
                .name("Student Name")
                .email(email)
                .course("Computer Science")
                .division("A")
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
