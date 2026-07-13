package com.cts.mapper;

import com.cts.dto.*;
import com.cts.entity.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class RegistrarMapper {

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
}