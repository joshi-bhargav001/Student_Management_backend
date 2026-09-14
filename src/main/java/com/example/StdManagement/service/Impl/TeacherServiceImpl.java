package com.example.StdManagement.service.Impl;

import com.example.StdManagement.dto.Request.TeacherRequest;
import com.example.StdManagement.dto.Response.TeacherResponse;
import com.example.StdManagement.dto.Response.TeacherUserResponse;
import com.example.StdManagement.entity.Teacher;
import com.example.StdManagement.exception.DuplicateResourceException;
import com.example.StdManagement.exception.ResourceNotFoundException;
import com.example.StdManagement.repository.TeacherRepository;
import com.example.StdManagement.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;

    @Override
    public TeacherResponse createTeacher(TeacherRequest request) {
        if(teacherRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Teacher email is already exists");
        }

        Teacher teacher = Teacher.builder()
                .name(request.getName())
                .email(request.getEmail())
                .number(request.getNumber())
                .address(request.getAddress())
                .gender(request.getGender())
                .salary(request.getSalary())
                .subject(request.getSubject())
                .experience(request.getExperience())
                .build();

        Teacher saveData = teacherRepository.save(teacher);
        return mapToResponse(saveData);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeacher() {
        return teacherRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherUserResponse> getAllTeachers() {
        return teacherRepository.findAll()
                .stream()
                .map(teacher -> TeacherUserResponse.builder()
                        .id(teacher.getId())
                        .name(teacher.getName())
                        .email(teacher.getEmail())
                        .gender(teacher.getGender())
                        .subject(teacher.getSubject())
                        .experience(teacher.getExperience())
                        .build())
                .toList();
    }

    @Override
    public TeacherResponse updateTeacher(Long id,TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Teacher not found with id"));

        if(teacherRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("Teacher email is already exist" + request.getEmail());
        }

        teacher.setName(request.getName());
        teacher.setEmail(request.getEmail());
        teacher.setAddress(request.getAddress());
        teacher.setGender(request.getGender());
        teacher.setExperience(request.getExperience());
        teacher.setNumber(request.getNumber());
        teacher.setSalary(request.getSalary());
        teacher.setSubject(request.getSubject());

        Teacher saveTeacher = teacherRepository.save(teacher);
        return mapToResponse(saveTeacher);
    }

    @Override
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Teacher not found for delete id" + id));
        teacherRepository.delete(teacher);
    }

    @Override
    public List<Teacher> searchTeacher(String keyword) {
        String searchTerm = keyword == null ? "" : keyword.trim();

        if(searchTerm.isEmpty()) {
            return List.of();
        }
        return teacherRepository.findByNameContainingIgnoreCaseOrSubjectContainingIgnoreCase(searchTerm, searchTerm);
    }

    @Override
    public Page<TeacherResponse> getAllPage(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Teacher> teachers = teacherRepository.findAll(pageable);

        return teachers.map(this::mapToResponse);
    }

    private TeacherResponse mapToResponse(Teacher teacher) {
        return TeacherResponse.builder()
                .id(teacher.getId())
                .name(teacher.getName())
                .email(teacher.getEmail())
                .number(teacher.getNumber())
                .address(teacher.getAddress())
                .gender(teacher.getGender())
                .salary(teacher.getSalary())
                .subject(teacher.getSubject())
                .experience(teacher.getExperience())
                .build();
    }
}
