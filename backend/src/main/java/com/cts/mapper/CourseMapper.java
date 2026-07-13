package com.cts.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import com.cts.dto.CourseMaterialFileOutputDTO;
import com.cts.dto.CourseOutputDTO;
import com.cts.entity.Course;
import com.cts.entity.CourseMaterialFile;
import lombok.AllArgsConstructor;


@Component
@AllArgsConstructor
public class CourseMapper {


    public CourseOutputDTO toCourseOutputDTO(Course course) {
        return CourseOutputDTO.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .description(course.getDescription())
                .prerequisite(course.getPrerequisite())
                .instructorId(course.getInstructor() != null
                        ? course.getInstructor().getInstructorId() : null)
                .instructorName(course.getInstructor() != null
                        && course.getInstructor().getUser() != null
                        ? course.getInstructor().getUser().getName() : null)
                .build();
    }

    public CourseMaterialFileOutputDTO toCourseMaterialFileOutputDTO(CourseMaterialFile f) {
        return CourseMaterialFileOutputDTO.builder()
                .fileId(f.getFileId())
                .courseId(f.getCourse().getCourseId())
                .courseTitle(f.getCourse().getTitle())
                .fileName(f.getFileName())
                .type(f.getType() != null ? f.getType() : "PDF")
                .textContent(f.getTextContent())
                .uploadedAt(f.getUploadedAt())
                .build();
    }

}
