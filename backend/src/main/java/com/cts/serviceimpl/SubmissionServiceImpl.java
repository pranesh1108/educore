package com.cts.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import com.cts.annotation.AuditEvent;
import com.cts.dto.GradeInputDTO;
import com.cts.dto.SubmissionOutputDTO;
import com.cts.entity.Course;
import com.cts.entity.Instructor;
import com.cts.entity.Submission;
import com.cts.exception.CourseNotAssignedToInstructorException;
import com.cts.exception.CourseNotFoundException;
import com.cts.exception.InstructorNotFoundException;
import com.cts.exception.InvalidFileException;
import com.cts.exception.InvalidGradeException;
import com.cts.exception.SubmissionNotFoundException;
import com.cts.mapper.StudentMapper;
import com.cts.repository.CourseRepository;
import com.cts.repository.InstructorRepository;
import com.cts.repository.SubmissionRepository;
import com.cts.service.FileStorageService;
import com.cts.service.SubmissionService;
import com.cts.util.SecurityUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final StudentMapper studentMapper;
    private final SubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final FileStorageService fileStorageService;

    private Instructor getLoggedInInstructor() {
        String loggedInEmail = SecurityUtils.getLoggedInEmail();
        return instructorRepository
                .findByUser_Email(loggedInEmail)
                .orElseThrow(() -> new InstructorNotFoundException(
                        "Instructor profile not found for logged-in user."));
    }

    @Override
    @AuditEvent(eventName = "SUBMISSIONS_FETCHED", eventType = "READ", eventMessage = "Instructor fetched student submissions for a course")
    public List<SubmissionOutputDTO> getSubmissionsForCourse(Long courseId) {
        Instructor instructor = getLoggedInInstructor();
        verifyOwnership(instructor.getInstructorId(), courseId);

        List<Submission> submissions = submissionRepository.findByCourse_CourseId(courseId);
        if (submissions.isEmpty()) {
            throw new SubmissionNotFoundException("No submissions found for course id: " + courseId);
        }
        return submissions.stream().map(studentMapper::toSubmissionOutputDTO).collect(Collectors.toList());
    }

    @Override
    @AuditEvent(eventName = "SUBMISSION_FILE_DOWNLOADED", eventType = "READ", eventMessage = "Instructor downloaded a student submission file")
    public byte[] downloadSubmissionFile(Long submissionId) {
        Instructor instructor = getLoggedInInstructor();
        Submission submission = getVerifiedSubmission(instructor.getInstructorId(), submissionId);

        if (submission.getFilePath() == null || submission.getFilePath().isBlank()) {
            throw new InvalidFileException("No PDF file found for submission id: " + submissionId);
        }
        return fileStorageService.loadFile(submission.getFilePath());
    }

    @Override
    public String getSubmissionFileName(Long submissionId) {
        Instructor instructor = getLoggedInInstructor();
        return getVerifiedSubmission(instructor.getInstructorId(), submissionId).getFileName();
    }

    @Override
    @AuditEvent(eventName = "SUBMISSION_GRADED", eventType = "UPDATE", eventMessage = "Instructor graded a student submission")
    public SubmissionOutputDTO gradeSubmission(Long submissionId, GradeInputDTO gradeInputDTO) {
        Instructor instructor = getLoggedInInstructor();

        if (gradeInputDTO.getGrade() == null || gradeInputDTO.getGrade() < 0.0 || gradeInputDTO.getGrade() > 100.0) {
            throw new InvalidGradeException("Grade must be between 0.0 and 100.0");
        }

        Submission submission = getVerifiedSubmission(instructor.getInstructorId(), submissionId);
        submission.setGrade(gradeInputDTO.getGrade());
        submission.setFeedback(gradeInputDTO.getFeedback());
        submission.setGradedAt(LocalDateTime.now());
        submission.setStatus("GRADED");
        return studentMapper.toSubmissionOutputDTO(submissionRepository.save(submission));
    }

    private Submission getVerifiedSubmission(Long instructorId, Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException(
                        "Submission not found with id: " + submissionId));
        verifyOwnership(instructorId, submission.getCourse().getCourseId());
        return submission;
    }

    private void verifyOwnership(Long instructorId, Long courseId) {
        // FIXED: Removed redundant instructor existence db check here since getLoggedInInstructor handles it beforehand
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));
        if (course.getInstructor() == null || !course.getInstructor().getInstructorId().equals(instructorId)) {
            throw new CourseNotAssignedToInstructorException(
                    "Course " + courseId + " is not assigned to instructor " + instructorId);
        }
    }
}