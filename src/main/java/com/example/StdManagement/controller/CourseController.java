package com.example.StdManagement.controller;

import com.example.StdManagement.dto.Request.CourseRequest;
import com.example.StdManagement.dto.Response.ApiResponse;
import com.example.StdManagement.dto.Response.CourseResponse;
import com.example.StdManagement.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<?> allCourse(@RequestParam(required = false) Boolean dropdown) {
        if (Boolean.TRUE.equals(dropdown)) {
            return ResponseEntity.ok(courseService.getCourseDropdown());
        }
        return ResponseEntity.ok(courseService.allCourse());
    }

    @GetMapping("/pages")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<CourseResponse>> getCoursePage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return ResponseEntity.ok(courseService.getCoursePage(page, size));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<CourseResponse>> searchCourse(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String department) {
        String searchTerm = firstNonBlank(keyword, courseName, department);

        if (searchTerm.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        return ResponseEntity.ok(courseService.searchCourse(searchTerm));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Course deleted successfully")
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
