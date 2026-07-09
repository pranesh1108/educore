package com.cts.repository;

import com.cts.entity.ExamResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResultEntity, Long> {

    List<ExamResultEntity> findByStudent_StudentId(Long studentId);

    Optional<ExamResultEntity> findByExam_ExamIdAndStudent_StudentId(Long examId, Long studentId);

    boolean existsByExam_ExamIdAndStudent_StudentId(Long examId, Long studentId);

    List<ExamResultEntity> findByExam_ExamId(Long examId);

    boolean existsByStudent_StudentIdAndCourse_CourseId(Long studentId, Long courseId);
}
