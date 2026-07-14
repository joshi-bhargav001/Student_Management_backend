package com.example.StdManagement.service;

import com.example.StdManagement.dto.Request.TeacherRequest;
import com.example.StdManagement.dto.Response.TeacherResponse;
import com.example.StdManagement.dto.Response.TeacherUserResponse;

import java.util.List;

public interface TeacherService {
    TeacherResponse createTeacher(TeacherRequest request);

    List<TeacherResponse> getAllTeacher();

    List<TeacherUserResponse> getAllTeachers();

    TeacherResponse updateTeacher(Long id,TeacherRequest request);

    void deleteTeacher(Long id);
}
