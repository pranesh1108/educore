package com.cts.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import com.cts.entity.*;
import com.cts.exception.*;
import com.cts.repository.*;
import com.cts.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cts.annotation.AuditEvent;
import com.cts.dto.AssignmentInputDTO;
import com.cts.dto.AssignmentOutputDTO;
import com.cts.mapper.AssignmentMapper;
import com.cts.service.AssignmentService;
import com.cts.service.FileStorageService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentMapper assignmentMapper;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentFileRepository assignmentFileRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final FileStorageService fileStorageService;
    private final ExamResultRepository examResultRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    private Instructor getLoggedInInstructor() {
        String loggedInEmail = SecurityUtils.getLoggedInEmail();
        return instructorRepository
                .findByUser_Email(loggedInEmail)
                .orElseThrow(() -> new InstructorNotFoundException(
                        "Instructor profile not found for logged-in user context."));
    }

    @Override
    @AuditEvent(eventName = "ASSIGNMENT_PUBLISHED", eventType = "CREATE", eventMessage = "Instructor published a new assignment")
    public AssignmentOutputDTO publishAssignment(AssignmentInputDTO inputDTO, MultipartFile file) {

        Instructor instructor = getLoggedInInstructor();
        Long instructorId = instructor.getInstructorId();

        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse_CourseId(inputDTO.getCourseId());
        for (CourseEnrollment enrollment : enrollments) {
            List<ExamResultEntity> studentResults = examResultRepository.findByStudent_StudentId(enrollment.getStudent().getStudentId());
            for (ExamResultEntity result : studentResults) {
                if (result.getCourse().getCourseId().equals(inputDTO.getCourseId())) {
                    throw new BusinessException("Operation Denied: Exam scores have been published for this course.");
                }
            }
        }

        Course course = verifyOwnership(instructorId, inputDTO.getCourseId());

        Assignment assignment = Assignment.builder()
                .title(inputDTO.getTitle())
                .instructions(inputDTO.getInstructions())
                .totalMarks(inputDTO.getTotalMarks())
                .dueDate(inputDTO.getDueDate())
                .course(course)
                .publishedAt(LocalDateTime.now())
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        List<AssignmentFile> filesCreated = new java.util.ArrayList<>();

        if (file != null && !file.isEmpty()) {
            int existingCount = assignmentFileRepository.countByCourse(course.getCourseId());
            String autoName = fileStorageService.generateAssignmentFileName(
                    course.getTitle(), existingCount + 1);
            String savedPath = fileStorageService.storeFile(file, "assignments", autoName);

            AssignmentFile assignmentFile = AssignmentFile.builder()
                    .assignment(saved)
                    .filePath(savedPath)
                    .fileName(autoName)
                    .uploadedAt(LocalDateTime.now())
                    .build();
            filesCreated.add(assignmentFileRepository.save(assignmentFile));
        }
        return assignmentMapper.toAssignmentOutputDTO(saved, filesCreated);
    }


    private Course verifyOwnership(Long instructorId, Long courseId) {
        if (!instructorRepository.existsById(instructorId)) {
            throw new InstructorNotFoundException("Instructor not found with id: " + instructorId);
        }
        return courseRepository
                .findByCourseIdAndInstructor_InstructorId(courseId, instructorId)
                .orElseThrow(() -> new CourseNotAssignedToInstructorException(
                        "Course " + courseId + " is not assigned to instructor " + instructorId));
    }
}