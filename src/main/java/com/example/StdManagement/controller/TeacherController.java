package com.example.StdManagement.controller;

import com.example.StdManagement.dto.Request.TeacherRequest;
import com.example.StdManagement.dto.Response.ApiResponse;
import com.example.StdManagement.dto.Response.TeacherResponse;
import com.example.StdManagement.dto.Response.TeacherUserResponse;
import com.example.StdManagement.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping("/teacher")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TeacherResponse> createTeacher(@RequestBody TeacherRequest request) {
        TeacherResponse createdTeacher = teacherService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeacher);
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<TeacherResponse>> getAllTeacher() {
        return ResponseEntity.ok(teacherService.getAllTeacher());
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<TeacherUserResponse>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @PutMapping("/teacher/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TeacherResponse> updateTeacher(@PathVariable Long id, @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(teacherService.updateTeacher(id,request));
    }

    @DeleteMapping("/teacher/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Teacher deleted successfully")
                .build());
    }
}
