package com.example.StdManagement.repository;

import com.example.StdManagement.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Teacher> findByNameContainingIgnoreCaseOrSubjectContainingIgnoreCase(String name, String subject);
}
