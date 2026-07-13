package com.cts.repository;

import com.cts.entity.ExamRoomAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamRoomAllocationRepository
        extends JpaRepository<ExamRoomAllocation, Long> {

    List<ExamRoomAllocation> findByExamRoom_RoomId(Long roomId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT a.student.studentId FROM ExamRoomAllocation a " +
            "WHERE a.exam.examId = :examId")
    List<Long> findAllocatedStudentIdsByExamId(
            @org.springframework.data.repository.query.Param("examId") Long examId);
}