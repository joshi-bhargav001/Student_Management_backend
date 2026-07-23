package com.example.StdManagement.service.Impl;

import com.example.StdManagement.dto.Request.StudentRequest;
import com.example.StdManagement.dto.Response.StudentResponse;
import com.example.StdManagement.entity.Student;
import com.example.StdManagement.exception.DuplicateResourceException;
import com.example.StdManagement.exception.ResourceNotFoundException;
import com.example.StdManagement.repository.StudentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.example.StdManagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Student with email already exists: " + request.getEmail());
        }
        if (studentRepository.existsByRollNo(request.getRollNo())) {
            throw new DuplicateResourceException("Student with roll number already exists: " + request.getRollNo());
        }

        Student student = Student.builder()
                .rollNo(request.getRollNo())
                .name(request.getName())
                .email(request.getEmail())
                .course(request.getCourse())
                .mobile(request.getMobile())
                .build();

        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = findStudentById(id);
        return mapToResponse(student);
    }

    @Override
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = findStudentById(id);

        if (studentRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("Student with email already exists: " + request.getEmail());
        }
        if (studentRepository.existsByRollNoAndIdNot(request.getRollNo(), id)) {
            throw new DuplicateResourceException("Student with roll number already exists: " + request.getRollNo());
        }

        student.setName(request.getName());
        student.setRollNo(request.getRollNo());
        student.setEmail(request.getEmail());
        student.setCourse(request.getCourse());
        student.setMobile(request.getMobile());

        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    @Override
    public void deleteStudent(Long id) {
        Student student = findStudentById(id);
        studentRepository.delete(student);
    }

    @Override
    public List<Student> searchStudent(String keyword) {
        String searchTerm = keyword == null ? "" : keyword.trim();

        if (searchTerm.isEmpty()) {
            return List.of();
        }

        return studentRepository.findByNameContainingIgnoreCaseOrCourseContainingIgnoreCase(searchTerm, searchTerm);
    }

    @Override
    public Page<StudentResponse> getAllPage(int page, int size,String sortBy, String direction) {

        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Student> students = studentRepository.findAll(pageable);

        return students.map(this::mapToResponse);
    }

    @Override
    public StudentResponse uploadPhoto(Long id, MultipartFile file) throws IOException {
        Student student = studentRepository.findById(id).orElseThrow(()->new RuntimeException("Student not found"));

        String uploadDir = "uploads";

        Path uploadPath = Paths.get(uploadDir);

        if(!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = id +" " + file.getOriginalFilename();

        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING);

        student.setPhoto(filePath.toString());

        studentRepository.save(student);

        return convertTOResponse(student);
    }

    private StudentResponse convertTOResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .course(student.getCourse())
                .mobile(student.getMobile())
                .rollNo(student.getRollNo())
                .photo(student.getPhoto())
                .build();
    }

    private Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .rollNo(student.getRollNo())
                .name(student.getName())
                .email(student.getEmail())
                .course(student.getCourse())
                .mobile(student.getMobile())
                .build();
    }
}
