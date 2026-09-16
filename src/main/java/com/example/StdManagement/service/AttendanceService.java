package com.example.StdManagement.service;

import com.example.StdManagement.dto.Request.AttendanceRequest;
import com.example.StdManagement.dto.Response.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    List<AttendanceResponse> markAttendance(AttendanceRequest request);

    AttendanceResponse updateAttendance(Long id, AttendanceRequest.AttendanceItem request);

    List<AttendanceResponse> getAttendance(LocalDate date, String course, String division);
}
