package com.cts.service;

import java.util.List;
import com.cts.dto.EnrollmentOutputDTO;
import com.cts.dto.ExamOutputDTO;
import com.cts.dto.InstructorInputDTO;
import com.cts.dto.InstructorOutputDTO;

public interface InstructorService {

    InstructorOutputDTO updateInstructorProfile(InstructorInputDTO inputDTO);

    InstructorOutputDTO getInstructorProfile();

    List<ExamOutputDTO> getMyExams();

    List<EnrollmentOutputDTO> getEnrolledStudents(Long courseId);
}