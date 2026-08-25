package com.example.StdManagement.controller;

import com.example.StdManagement.dto.Response.ApiResponse;
import com.example.StdManagement.dto.Request.StudentRequest;
import com.example.StdManagement.dto.Response.StudentResponse;
import com.example.StdManagement.entity.Student;
import com.example.StdManagement.service.StudentService;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    // Create a new student.
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse createdStudent = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    // Get all students.
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/divisions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<String>> getDivisionDropdown() {
        return ResponseEntity.ok(studentService.getDivisionDropdown());
    }

    @GetMapping("/course")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<StudentResponse>> getAllStudentsByCourse(
            @RequestParam String course) {
        return ResponseEntity.ok(studentService.getAllStudentsByCourse(course));
    }

    @GetMapping("/course-division")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<StudentResponse>> getAllStudentsByCourseAndDivision(
            @RequestParam String course,
            @RequestParam String division) {
        return ResponseEntity.ok(studentService.getAllStudentsByCourseAndDivision(course, division));
    }

    // Get one student by id.
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    // Update an existing student by id.
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    // Delete a student by id.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Student deleted successfully")
                .build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<Student>> searchStudent(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String course) {
        String searchTerm = firstNonBlank(keyword, name, course);

        if (searchTerm.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(studentService.searchStudent(searchTerm));
    }

    @GetMapping("/pages")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<StudentResponse>> getAllPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(studentService.getAllPage(page, size));
    }

    @PostMapping("/{id}/upload-photo")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StudentResponse> uploadPhoto(@PathVariable Long id, @RequestParam("file")MultipartFile file) throws IOException {
        StudentResponse response = studentService.uploadPhoto(id, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/photo")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> showPhoto(@PathVariable Long id) throws IOException {
        return studentService.showPhoto(id);
    }

    @DeleteMapping("/{id}/delete-photo")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deletePhoto(@PathVariable Long id) throws IOException {
        studentService.deletePhoto(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Photo deleted successfully")
                .build());
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }
}
