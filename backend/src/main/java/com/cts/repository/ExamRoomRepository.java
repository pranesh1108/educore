package com.cts.repository;

import com.cts.entity.ExamRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamRoomRepository extends JpaRepository<ExamRoom, Long> {

    // Check if a room name is already assigned to another exam
    // at the exact same date and time — conflict check
    @Query("SELECT COUNT(r) > 0 FROM ExamRoom r WHERE " +
           "r.roomName = :roomName AND " +
           "r.exam.examDate = :examDate AND " +
           "(:excludeExamId IS NULL OR r.exam.examId <> :excludeExamId)")
    boolean existsRoomConflict(@Param("roomName") String roomName,
                                @Param("examDate") LocalDateTime examDate,
                                @Param("excludeExamId") Long excludeExamId);

    // Find all rooms for a specific exam
    List<ExamRoom> findByExam_ExamId(Long examId);

    // Check if room number already used for this exam
    boolean existsByExam_ExamIdAndRoomNumber(Long examId, Integer roomNumber);
}