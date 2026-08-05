package com.example.StdManagement.service;

import com.example.StdManagement.dto.Request.StudentRequest;
import com.example.StdManagement.dto.Response.StudentResponse;
import com.example.StdManagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface StudentService{

    StudentResponse createStudent(StudentRequest request);

    List<StudentResponse> getAllStudents();

    StudentResponse getStudentById(Long id);

    StudentResponse updateStudent(Long id, StudentRequest request);

    void deleteStudent(Long id);

    List<Student> searchStudent(String keyword);

    Page<StudentResponse> getAllPage(int page, int size);

    StudentResponse uploadPhoto(Long id, MultipartFile file) throws IOException;

    ResponseEntity<byte[]> showPhoto(Long id) throws IOException;

    void deletePhoto(Long id) throws IOException;
}
