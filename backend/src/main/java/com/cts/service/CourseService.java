package com.cts.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.cts.dto.CourseMaterialFileOutputDTO;
import com.cts.dto.CourseOutputDTO;
import com.cts.dto.InstructorResourceResponseDTO;

public interface CourseService {

    List<CourseOutputDTO> getAssignedCourses();

    CourseMaterialFileOutputDTO publishCourseMaterial(Long courseId, MultipartFile file, String textContent);

    // ADDED: New unified endpoint contract method
    InstructorResourceResponseDTO getCourseResources(Long courseId);
}