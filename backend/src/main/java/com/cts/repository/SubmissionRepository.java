package com.cts.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cts.entity.Submission;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {


    List<Submission> findByCourse_CourseId(Long courseId);

    List<Submission> findByStudent_StudentId(Long studentId);

    List<Submission> findByStudent_StudentIdAndCourse_CourseId(Long studentId,
                                                                Long courseId);
}