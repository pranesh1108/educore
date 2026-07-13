package com.cts.repository;

import com.cts.entity.ExamRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamRoomRepository extends JpaRepository<ExamRoom, Long> {


    List<ExamRoom> findByExam_ExamId(Long examId);


    boolean existsByExam_ExamIdAndRoomNumber(Long examId, Integer roomNumber);
}