package com.cts.service;

import com.cts.dto.ExamResultInputDTO;
import com.cts.dto.ExamResultOutputDTO;
import com.cts.dto.ExamRoomAllocationStudentDTO;
import java.util.List;

public interface ExamResultService {

    ExamResultOutputDTO publishResult(ExamResultInputDTO inputDTO);

    // Removed explicit Long studentId argument
    List<ExamResultOutputDTO> getResultsByStudent();

    List<ExamRoomAllocationStudentDTO> getEnrolledStudentsForExam(Long examId);
}