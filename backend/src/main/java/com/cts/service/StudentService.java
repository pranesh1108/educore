package com.cts.service;

import com.cts.dto.*;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.core.io.Resource;


public interface StudentService {

    StudentOutputDTO updateStudentProfile(StudentInputDTO inputDTO);
    StudentOutputDTO getStudentProfile();
    List<RegistrarCourseResponseDTO> getAllCourses();

    Page<RegistrarCourseResponseDTO> filterCourses(String title, String topic, org.springframework.data.domain.Pageable pageable);
    EnrollmentOutputDTO enrollInCourse(Long courseId);
    List<EnrollmentOutputDTO> getMyEnrolledCourses();

    CourseContentResponseDTO getCourseContent(Long courseId);

    byte[] downloadCourseMaterialFile(Long fileId);
    String getCourseMaterialFileName(Long fileId);
    byte[] downloadAssignmentFile(Long fileId);
    String getAssignmentFileName(Long fileId);
    SubmissionOutputDTO submitAssignment(Long assignmentId, MultipartFile file);
    List<SubmissionOutputDTO> getMySubmissions();
    List<ExamOutputDTO> getMyExams();

    Resource getSyllabusResource(Long courseId);

}