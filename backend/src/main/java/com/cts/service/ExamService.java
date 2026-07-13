package com.cts.service;

import com.cts.dto.ExamInputDTO;
import com.cts.dto.ExamOutputDTO;
import com.cts.dto.InstructorOutputDTO;
import java.util.List;

public interface ExamService {

    ExamOutputDTO createExam(ExamInputDTO inputDTO);

    List<ExamOutputDTO> searchExams(Long courseId, Long instructorId, String term, String status);

    ExamOutputDTO getExamById(Long examId);

    void deleteExam(Long examId);

    List<InstructorOutputDTO> getAllInstructors();
}
