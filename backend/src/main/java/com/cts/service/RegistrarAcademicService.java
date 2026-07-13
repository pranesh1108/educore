package com.cts.service;

import com.cts.dto.*;
import java.util.List;

public interface RegistrarAcademicService {

    RegistrarCourseResponseDTO provisionNewCourse(RegistrarCourseCreateDTO createDTO);

    List<RegistrarCourseResponseDTO> getAllConfiguredCourses();

    Object filterByRole(String role, String name, String status,
                        String fieldOfInterest, String enrolledCourse,
                        String skill, Integer experience,
                        String sortBy, String sortDir);
}
