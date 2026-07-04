package com.example.StdManagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student full name.
    @Column(nullable = false)
    private String name;

    // Email is unique so two students cannot use the same email address.
    @Column(nullable = false, unique = true)
    private String email;

    // Course or program the student is enrolled in.
    @Column(nullable = false)
    private String course;

    // Mobile number is kept as text because phone numbers are not used for math.
    @Column(nullable = false, length = 15)
    private String mobile;

    // Roll number is required for each student.
    @Column(name = "rollno", nullable = false, unique = true, length = 20)
    private String rollNo;
}
