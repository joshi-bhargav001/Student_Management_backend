package com.example.StdManagement.repository;

import com.example.StdManagement.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course,Long > {

    Boolean existsByCourseName(String courseName);

    Boolean existsByCourseNameAndIdNot(String courseName, Long id);

    java.util.List<Course> findByCourseNameContainingIgnoreCaseOrDepartmentContainingIgnoreCase(
            String courseName, String department);
}
