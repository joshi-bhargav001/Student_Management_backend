package com.example.StdManagement.service;

import com.example.StdManagement.dto.Request.CourseRequest;
import com.example.StdManagement.dto.Response.CourseResponse;

import java.util.List;
import org.springframework.data.domain.Page;

public interface CourseService {

    CourseResponse createCourse(CourseRequest request);

    List<CourseResponse> allCourse();

    List<String> getCourseDropdown();

    Page<CourseResponse> getCoursePage(int page, int size);

    CourseResponse updateCourse(Long id, CourseRequest request);

    void deleteCourse(Long id);

    List<CourseResponse> searchCourse(String keyword);
}
