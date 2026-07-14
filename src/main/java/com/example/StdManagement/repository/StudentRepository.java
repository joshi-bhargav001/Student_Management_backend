package com.example.StdManagement.repository;

import com.example.StdManagement.entity.Student;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByRollNo(String rollNo);

    boolean existsByRollNoAndIdNot(String rollNo, Long id);

    List<Student> findByNameContainingIgnoreCaseOrCourseContainingIgnoreCase(String name, String course);
}
