package com.cts.repository;

import com.cts.entity.ExamRoomAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamRoomAllocationRepository
        extends JpaRepository<ExamRoomAllocation, Long> {

    // Count how many students already allocated for this exam
    long countByExam_ExamId(Long examId);

    // Get all allocations for a specific exam
    List<ExamRoomAllocation> findByExam_ExamId(Long examId);

    // Get all allocations for a specific room
    List<ExamRoomAllocation> findByExamRoom_RoomId(Long roomId);

    // Check if a student is already allocated to any room for this exam
    boolean existsByStudent_StudentIdAndExam_ExamId(Long studentId, Long examId);

    // Get all student IDs already allocated for this exam
    // Used to determine which batch is next
    @org.springframework.data.jpa.repository.Query(
            "SELECT a.student.studentId FROM ExamRoomAllocation a " +
            "WHERE a.exam.examId = :examId")
    List<Long> findAllocatedStudentIdsByExamId(
            @org.springframework.data.repository.query.Param("examId") Long examId);
}