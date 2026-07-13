package com.cts.repository;

import com.cts.entity.Exam;
import com.cts.enumerate.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    @Query("SELECT e FROM Exam e WHERE " +
           "(:courseId IS NULL OR e.course.courseId = :courseId) AND " +
           "(:instructorId IS NULL OR e.instructor.instructorId = :instructorId) AND " +
           "(:status IS NULL OR e.status = :status)")
    List<Exam> searchExams(@Param("courseId") Long courseId,
                           @Param("instructorId") Long instructorId,
                           @Param("status") ExamStatus status);

    List<Exam> findByInstructor_InstructorId(Long instructorId);

    @Query("SELECT e FROM Exam e WHERE e.course.courseId IN " +
           "(SELECT ce.course.courseId FROM CourseEnrollment ce " +
           " WHERE ce.student.studentId = :studentId)")
    List<Exam> findExamsForStudent(@Param("studentId") Long studentId);


    List<Exam> findByCourse_CourseId(Long courseId);

    boolean existsByCourse_CourseId(Long courseId);
}
