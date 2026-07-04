package com.example.StdManagement.service;

import com.example.StdManagement.dto.StudentRequest;
import com.example.StdManagement.dto.StudentResponse;
import com.example.StdManagement.entity.Student;
import com.example.StdManagement.exception.DuplicateResourceException;
import com.example.StdManagement.exception.ResourceNotFoundException;
import com.example.StdManagement.repository.StudentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
