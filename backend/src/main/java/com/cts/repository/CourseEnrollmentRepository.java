package com.cts.repository;

import com.cts.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    boolean existsByStudent_StudentIdAndCourse_CourseId(Long studentId, Long courseId);

    List<CourseEnrollment> findByCourse_CourseId(Long courseId);

    List<CourseEnrollment> findByStudent_StudentId(Long studentId);

    Optional<CourseEnrollment> findByStudent_StudentIdAndCourse_CourseId(Long studentId, Long courseId);
}
