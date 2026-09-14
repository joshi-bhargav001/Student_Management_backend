package com.example.StdManagement.service.Impl;

import com.example.StdManagement.dto.Request.CourseRequest;
import com.example.StdManagement.dto.Response.CourseResponse;
import com.example.StdManagement.entity.Course;
import com.example.StdManagement.exception.DuplicateResourceException;
import com.example.StdManagement.exception.ResourceNotFoundException;
import com.example.StdManagement.repository.CourseRepository;
import com.example.StdManagement.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public CourseResponse createCourse(CourseRequest request) {
        if(courseRepository.existsByCourseName(request.getCourseName())) {
            throw new DuplicateResourceException("Course already exist " + request.getCourseName());
        }

        Course course = Course.builder()
                .courseName(request.getCourseName())
                .duration(request.getDuration())
                .totalSemester(request.getTotalSemester())
                .department(request.getDepartment())
                .status(request.getStatus())
                .createAt(LocalDateTime.now())
                .build();
        Course saveCourse = courseRepository.save(course);
        return mapToResponse(saveCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> allCourse() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCourseDropdown() {
        return courseRepository.findAll()
                .stream()
                .map(Course::getCourseName)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseResponse> getCoursePage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return courseRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("course not found with id" + id));

        if(courseRepository.existsByCourseNameAndIdNot(request.getCourseName(), id)) {
            throw new DuplicateResourceException("Course is already exists " + request.getCourseName());
        }

        course.setCourseName(request.getCourseName());
        course.setDuration(request.getDuration());
        course.setTotalSemester(request.getTotalSemester());
        course.setDepartment(request.getDepartment());
        course.setStatus(request.getStatus());

        Course updatedCourse = courseRepository.save(course);
        return mapToResponse(updatedCourse);
    }

    @Override
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("course not found with id " + id));
        courseRepository.delete(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> searchCourse(String keyword) {
        String searchTerm = keyword == null ? "" : keyword.trim();
        if (searchTerm.isBlank()) {
            return List.of();
        }

        return courseRepository
                .findByCourseNameContainingIgnoreCaseOrDepartmentContainingIgnoreCase(searchTerm, searchTerm)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .duration(course.getDuration())
                .totalSemester(course.getTotalSemester())
                .department(course.getDepartment())
                .status(course.getStatus())
                .createAt(course.getCreateAt())
                .build();
    }

}
