package com.cts.service;

import java.util.List;
import com.cts.dto.GradeInputDTO;
import com.cts.dto.SubmissionOutputDTO;

public interface SubmissionService {

    List<SubmissionOutputDTO> getSubmissionsForCourse(Long courseId);

    SubmissionOutputDTO gradeSubmission(Long submissionId, GradeInputDTO gradeInputDTO);

    byte[] downloadSubmissionFile(Long submissionId);

    String getSubmissionFileName(Long submissionId);
}