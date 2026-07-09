package com.cts.service;

import com.cts.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RegistrarAcademicService {

    // Creates course AND assigns instructor — checks instructor profile completeness
    RegistrarCourseResponseDTO provisionNewCourse(RegistrarCourseCreateDTO createDTO);

    // Get all courses — shared catalogue
    List<RegistrarCourseResponseDTO> getAllConfiguredCourses();

    RegistrarCourseResponseDTO provisionNewCourseWithSyllabus(String courseDataJson, MultipartFile syllabusFile);

    // Filter students or instructors with optional fields and sorting
    // role = "student" or "instructor"
    Object filterByRole(String role, String name, String status,
                        String fieldOfInterest, String enrolledCourse,
                        String skill, Integer experience,
                        String sortBy, String sortDir);
}
