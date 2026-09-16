package com.example.StdManagement.repository;

import com.example.StdManagement.entity.Student;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByRollNo(String rollNo);

    boolean existsByRollNoAndIdNot(String rollNo, Long id);

    List<Student> findByCourseIgnoreCase(String course);

    List<Student> findByCourseIgnoreCaseAndDivisionIgnoreCase(String course, String division);

    List<Student> findByNameContainingIgnoreCaseOrCourseContainingIgnoreCase(String name, String course);

    @Query("select distinct s.division from Student s where s.division is not null and trim(s.division) <> '' order by s.division")
    List<String> findDistinctDivisions();

    @Query("select count(distinct s.division) from Student s where s.division is not null and trim(s.division) <> ''")
    long countDistinctDivisions();

    @Query("""
            select c.courseName as course, count(s.id) as students
            from Course c
            left join Student s on lower(trim(s.course)) = lower(trim(c.courseName))
            group by c.id, c.courseName
            order by count(s.id) desc, c.courseName
            """)
    List<CourseWiseStudentCount> countStudentsByCourse();

    interface CourseWiseStudentCount {

        String getCourse();

        long getStudents();
    }
}
