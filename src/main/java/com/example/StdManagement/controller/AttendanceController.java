package com.example.StdManagement.controller;

import com.example.StdManagement.dto.Request.AttendanceRequest;
import com.example.StdManagement.dto.Response.AttendanceResponse;
import com.example.StdManagement.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<AttendanceResponse>> getAttendance(
            @RequestParam LocalDate date,
            @RequestParam String course,
            @RequestParam String division) {
        return ResponseEntity.ok(attendanceService.getAttendance(date, course, division));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AttendanceResponse>> markAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.markAttendance(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest.AttendanceItem request) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, request));
    }
}
