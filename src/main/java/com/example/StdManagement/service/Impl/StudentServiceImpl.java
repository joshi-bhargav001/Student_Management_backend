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
import java.nio.file.NoSuchFileException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.example.StdManagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

        String rollNo = generateNextRollNo();

        Student student = Student.builder()
                .rollNo(rollNo)
                .name(request.getName())
                .email(request.getEmail())
                .course(request.getCourse())
                .division(request.getDivision())
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
    public List<StudentResponse> getAllStudentsByCourse(String course) {
        String searchTerm = course == null ? "" : course.trim();

        if (searchTerm.isEmpty()) {
            return getAllStudents();
        }

        return studentRepository.findByCourseIgnoreCase(searchTerm)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudentsByCourseAndDivision(String course, String division) {
        String courseTerm = course == null ? "" : course.trim();
        String divisionTerm = division == null ? "" : division.trim();

        if (courseTerm.isEmpty() || divisionTerm.isEmpty()) {
            return getAllStudents();
        }

        return studentRepository
                .findByCourseIgnoreCaseAndDivisionIgnoreCase(courseTerm, divisionTerm)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDivisionDropdown() {
        return studentRepository.findDistinctDivisions();
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

        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setCourse(request.getCourse());
        student.setDivision(request.getDivision());
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
    public Page<StudentResponse> getAllPage(int page, int size) {

//        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size);

        Page<Student> students = studentRepository.findAll(pageable);

        return students.map(this::mapToResponse);
    }

    @Override
    public StudentResponse uploadPhoto(Long id, MultipartFile file) throws IOException {
        Student student = studentRepository.findById(id).orElseThrow(()->new RuntimeException("Student not found"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

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

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> showPhoto(Long id) throws IOException {
        Student student = findStudentById(id);

        if (student.getPhoto() == null || student.getPhoto().isBlank()) {
            throw new ResourceNotFoundException("Photo not found for student id: " + id);
        }

        Path photoPath = Paths.get(student.getPhoto());
        if (!Files.exists(photoPath)) {
            throw new ResourceNotFoundException("Photo file not found for student id: " + id);
        }

        byte[] imageBytes = Files.readAllBytes(photoPath);
        String contentType = Files.probeContentType(photoPath);

        MediaType mediaType = contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(imageBytes);
    }

    @Override
    public void deletePhoto(Long id) throws IOException {
        Student student = findStudentById(id);

        if (student.getPhoto() == null || student.getPhoto().isBlank()) {
            throw new ResourceNotFoundException("Photo not found for student id: " + id);
        }

        Path photoPath = Paths.get(student.getPhoto());

        try {
            Files.deleteIfExists(photoPath);
        } catch (NoSuchFileException exception) {
            throw new ResourceNotFoundException("Photo file not found for student id: " + id);
        }

        student.setPhoto(null);
        studentRepository.save(student);
    }

    private StudentResponse convertTOResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .course(student.getCourse())
                .division(student.getDivision())
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
                .division(student.getDivision())
                .mobile(student.getMobile())
                .photo(student.getPhoto())
                .build();
    }

    private String generateNextRollNo() {
        String prefix = "R";

        int nextNumber = studentRepository.findAll().stream()
                .map(Student::getRollNo)
                .filter(rollNo -> rollNo != null && rollNo.matches("^R\\d+$"))
                .map(rollNo -> rollNo.substring(1))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0) + 1;

        String candidate = formatRollNo(prefix, nextNumber);
        while (studentRepository.existsByRollNo(candidate)) {
            nextNumber++;
            candidate = formatRollNo(prefix, nextNumber);
        }

        return candidate;
    }

    private String formatRollNo(String prefix, int number) {
        return prefix + String.format("%03d", number);
    }
}
