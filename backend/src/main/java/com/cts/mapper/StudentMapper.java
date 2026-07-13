package com.cts.mapper;

import org.springframework.stereotype.Component;
import com.cts.dto.*;
import com.cts.entity.*;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class StudentMapper {


    public RegistrarCourseResponseDTO toRegistrarCourseResponseDTO(Course course) {
        RegistrarCourseResponseDTO.RegistrarCourseResponseDTOBuilder builder =
                RegistrarCourseResponseDTO.builder()
                        .courseId(course.getCourseId())
                        .title(course.getTitle())
                        .description(course.getDescription())
                        .prerequisite(course.getPrerequisite())
                        .startDate(course.getStartDate())
                        .endDate(course.getEndDate())
                        .enrollmentDeadlineDate(course.getEnrollmentDeadlineDate())
                        .syllabusPath(course.getSyllabusPath());

        if (course.getInstructor() != null) {
            builder.instructorId(course.getInstructor().getInstructorId());

            String flattenedSkills = (course.getInstructor().getSkills() != null && !course.getInstructor().getSkills().isEmpty())
                    ? course.getInstructor().getSkills().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "))
                    : null;

            builder.instructorSkill(flattenedSkills);

            if (course.getInstructor().getUser() != null) {
                builder.instructorName(course.getInstructor().getUser().getName());
                builder.instructorEmail(course.getInstructor().getUser().getEmail());
            }
        }
        return builder.build();
    }

    public EnrollmentOutputDTO toEnrollmentOutputDTO(CourseEnrollment e) {
        String instructorName = null;
        if (e.getCourse().getInstructor() != null
                && e.getCourse().getInstructor().getUser() != null) {
            instructorName = e.getCourse().getInstructor().getUser().getName();
        }
        return EnrollmentOutputDTO.builder()
                .enrollmentId(e.getEnrollmentId())
                .courseId(e.getCourse().getCourseId())
                .courseTitle(e.getCourse().getTitle())
                .courseDescription(e.getCourse().getDescription())
                .instructorName(instructorName)
                .studentId(e.getStudent().getStudentId())
                .studentName(e.getStudent().getUser().getName())
                .enrollmentNumber(e.getEnrollmentNumber())
                .enrolledAt(e.getEnrolledAt())
                .startDate(e.getCourse().getStartDate())
                .endDate(e.getCourse().getEndDate())
                .build();
    }

    public StudentOutputDTO tostudentOutputDTO(Student s) {
        return StudentOutputDTO.builder()
                .studentId(s.getStudentId())
                .dateOfBirth(s.getDateOfBirth())
                .fieldOfInterest(s.getFieldOfInterest())
                .userId(s.getUser().getUserId())
                .name(s.getUser().getName())
                .email(s.getUser().getEmail())
                .phone(s.getUser().getPhone())
                .role(s.getUser().getRole().name())
                .build();
    }

    public AssignmentOutputDTO toAssignmentOutputDTO(Assignment a, List<AssignmentFileOutputDTO> files) {
        return AssignmentOutputDTO.builder()
                .assignmentId(a.getAssignmentId())
                .title(a.getTitle())
                .instructions(a.getInstructions())
                .totalMarks(a.getTotalMarks())
                .publishedAt(a.getPublishedAt())
                .dueDate(a.getDueDate())
                .courseId(a.getCourse().getCourseId())
                .courseTitle(a.getCourse().getTitle())
                .files(files)
                .build();
    }

    public SubmissionOutputDTO toSubmissionOutputDTO(Submission s) {
        String enrollmentNumber = null;
        if (s.getStudent() != null && s.getCourse() != null) {
            enrollmentNumber = s.getEnrollmentNumber();
        }
        return SubmissionOutputDTO.builder()
                .submissionId(s.getSubmissionId())
                .studentId(s.getStudent().getStudentId())
                .studentName(s.getStudent().getUser().getName())
                .enrollmentNumber(enrollmentNumber)
                .fileName(s.getFileName())
                .submittedAt(s.getSubmittedAt())
                .status(s.getStatus())
                .grade(s.getGrade())
                .feedback(s.getFeedback())
                .assignmentId(s.getAssignment().getAssignmentId())
                .assignmentTitle(s.getAssignment().getTitle())
                .courseId(s.getCourse().getCourseId())
                .courseTitle(s.getCourse().getTitle())
                .build();
    }
}