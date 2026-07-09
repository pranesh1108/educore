package com.cts.repository;

import com.cts.entity.AssignmentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentFileRepository extends JpaRepository<AssignmentFile, Long> {

    List<AssignmentFile> findByAssignment_AssignmentId(Long assignmentId);

    @Query("SELECT COUNT(af) FROM AssignmentFile af WHERE af.assignment.course.courseId = :courseId")
    int countByCourse(@Param("courseId") Long courseId);
}
