package com.cts.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.cts.entity.*;
import com.cts.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cts.annotation.AuditEvent;
import com.cts.dto.CourseMaterialFileOutputDTO;
import com.cts.dto.CourseOutputDTO;
import com.cts.dto.InstructorResourceResponseDTO;
import com.cts.dto.AssignmentFileOutputDTO;
import com.cts.exception.BusinessException;
import com.cts.exception.CourseNotAssignedToInstructorException;
import com.cts.exception.CourseNotFoundException;
import com.cts.exception.InstructorNotFoundException;
import com.cts.mapper.CourseMapper;
import com.cts.mapper.AssignmentMapper;
import com.cts.service.CourseService;
import com.cts.service.FileStorageService;
import com.cts.util.SecurityUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final AssignmentMapper assignmentMapper;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final CourseMaterialFileRepository materialFileRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentFileRepository assignmentFileRepository;
    private final FileStorageService fileStorageService;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final ExamResultRepository examResultRepository;

    private Instructor getLoggedInInstructor() {
        String loggedInEmail = SecurityUtils.getLoggedInEmail();
        return instructorRepository
                .findByUser_Email(loggedInEmail)
                .orElseThrow(() -> new InstructorNotFoundException(
                        "Instructor profile not found for logged-in user context."));
    }

    @Override
    @AuditEvent(eventName = "ASSIGNED_COURSES_FETCHED", eventType = "READ", eventMessage = "Instructor fetched their assigned courses")
    public List<CourseOutputDTO> getAssignedCourses() {
        Instructor instructor = getLoggedInInstructor();
        List<Course> courses = courseRepository.findByInstructor_InstructorId(instructor.getInstructorId());
        if (courses.isEmpty()) {
            throw new CourseNotFoundException("No courses assigned to instructor id: " + instructor.getInstructorId());
        }
        return courses.stream().map(courseMapper::toCourseOutputDTO).collect(Collectors.toList());
    }

    @Override
    @AuditEvent(eventName = "COURSE_MATERIAL_PUBLISHED", eventType = "CREATE", eventMessage = "Instructor published course material")
    public CourseMaterialFileOutputDTO publishCourseMaterial(Long courseId,
                                                             MultipartFile file,
                                                             String textContent) {
        Instructor instructor = getLoggedInInstructor();
        Course course = verifyOwnership(instructor.getInstructorId(), courseId);

        LocalDate today = LocalDate.now();
        if (course.getStartDate() != null && today.isBefore(course.getStartDate())) {
            throw new BusinessException("Operation Denied: Cannot publish materials before the course start date.");
        }
        if (course.getEndDate() != null && today.isAfter(course.getEndDate())) {
            throw new BusinessException("Operation Denied: Cannot publish materials after the course has ended.");
        }

        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse_CourseId(courseId);
        for (CourseEnrollment enrollment : enrollments) {
            List<ExamResultEntity> studentResults = examResultRepository.findByStudent_StudentId(enrollment.getStudent().getStudentId());
            for (ExamResultEntity result : studentResults) {
                if (result.getCourse().getCourseId().equals(courseId)) {
                    throw new BusinessException("Operation Denied: Exam scores have been published for this course.");
                }
            }
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException("PDF file is mandatory for course material upload.");
        }

        if (textContent != null && textContent.length() > 50000) {
            throw new BusinessException("Text content cannot exceed 50,000 characters.");
        }

        int existingCount = materialFileRepository.findByCourse_CourseId(courseId).size();
        String autoName = fileStorageService.generateMaterialFileName(course.getTitle(), existingCount + 1);
        String savedPath = fileStorageService.storeFile(file, "materials", autoName);

        CourseMaterialFile materialFile = CourseMaterialFile.builder()
                .course(course)
                .filePath(savedPath)
                .fileName(autoName)
                .type("PDF")
                .textContent(textContent)
                .uploadedAt(LocalDateTime.now())
                .build();
        courseRepository.save(course);

        CourseMaterialFile saved = materialFileRepository.save(materialFile);
        return courseMapper.toCourseMaterialFileOutputDTO(saved);
    }

    @Override
    @AuditEvent(eventName = "COURSE_RESOURCES_VIEWED", eventType = "READ", eventMessage = "Instructor viewed course material and assignment files simultaneously")
    public InstructorResourceResponseDTO getCourseResources(Long courseId) {
        Instructor instructor = getLoggedInInstructor();
        verifyOwnership(instructor.getInstructorId(), courseId);

        List<CourseMaterialFileOutputDTO> materials = materialFileRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(courseMapper::toCourseMaterialFileOutputDTO)
                .collect(Collectors.toList());

        List<Assignment> assignments = assignmentRepository.findByCourse_CourseId(courseId);

        List<AssignmentFileOutputDTO> assignmentFiles = assignments.stream()
                .flatMap(assignment -> assignmentFileRepository.findByAssignment_AssignmentId(assignment.getAssignmentId()).stream())
                .map(assignmentMapper::toAssignmentFileOutputDTO)
                .collect(Collectors.toList());

        return InstructorResourceResponseDTO.builder()
                .materials(materials)
                .assignmentFiles(assignmentFiles)
                .build();
    }

    private Course verifyOwnership(Long instructorId, Long courseId) {
        return courseRepository
                .findByCourseIdAndInstructor_InstructorId(courseId, instructorId)
                .orElseThrow(() -> new CourseNotAssignedToInstructorException(
                        "Course " + courseId + " is not assigned to instructor " + instructorId));
    }
}