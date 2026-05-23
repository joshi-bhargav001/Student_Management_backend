package com.example.StdManagement.service;

import com.example.StdManagement.dto.StudentRequest;
import com.example.StdManagement.dto.StudentResponse;
import java.util.List;

public interface StudentService {

    StudentResponse createStudent(StudentRequest request);

    List<StudentResponse> getAllStudents();

    StudentResponse getStudentById(Long id);

    StudentResponse updateStudent(Long id, StudentRequest request);

    void deleteStudent(Long id);
}
