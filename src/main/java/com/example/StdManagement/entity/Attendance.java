package com.example.StdManagement.entity;

import com.example.StdManagement.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "date"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}
