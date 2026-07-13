package com.cts.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cts.dto.*;
import com.cts.exception.BusinessException;
import com.cts.service.RegistrarAcademicService;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import java.util.Set;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/registrar")
@PreAuthorize("hasRole('REGISTRAR')")
@Tag(name = "Registrar", description = "Course creation and filter endpoints")
public class RegistrarController {

    private final RegistrarAcademicService academicService;

    private static final Set<String> STUDENT_SORT_FIELDS =
            Set.of("name", "fieldofinterest", "status");
    private static final Set<String> INSTRUCTOR_SORT_FIELDS =
            Set.of("name", "experience", "status");
    private static final Set<String> SORT_DIRECTIONS = Set.of("asc", "desc");


    @Operation(
            summary = "Get all configured courses",
            description = "Returns every course currently configured in the system, published or not."
    )
    @GetMapping("/course")
    public ResponseEntity<List<RegistrarCourseResponseDTO>> getAllCourses() {
        return new ResponseEntity<>(academicService.getAllConfiguredCourses(), HttpStatus.OK);
    }

    @Operation(
            summary = "Create a course and assign an instructor",
            description = "Provisions a new course. Instructor must have skill and experience filled. "
                    + "Fails with 409 if course title already exists."
    )
    @PostMapping("/course")
    public ResponseEntity<RegistrarCourseResponseDTO> provisionNewCourse(
            @Valid @RequestBody RegistrarCourseCreateDTO createDTO) {
        return new ResponseEntity<>(
                academicService.provisionNewCourse(createDTO), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Filter students or instructors",
            description = "Filter by role (student or instructor) with optional fields. "
                    + "Student sortBy: name | fieldOfInterest | status. "
                    + "Instructor sortBy: name | experience | status. "
                    + "sortDir: asc (default) | desc."
    )
    @GetMapping("/{role}/filter")
    public ResponseEntity<Object> filterByRole(
            @PathVariable String role,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fieldOfInterest,
            @RequestParam(required = false) String enrolledCourse,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Integer experience,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {

        if (!SORT_DIRECTIONS.contains(sortDir.toLowerCase())) {
            throw new BusinessException(
                    "Invalid sortDir '" + sortDir + "'. Allowed values: asc, desc.");
        }

        if ("student".equalsIgnoreCase(role)
                && !STUDENT_SORT_FIELDS.contains(sortBy.toLowerCase())) {
            throw new BusinessException(
                    "Invalid sortBy '" + sortBy + "' for student. Allowed: name, fieldOfInterest, status.");
        }
        if ("instructor".equalsIgnoreCase(role)
                && !INSTRUCTOR_SORT_FIELDS.contains(sortBy.toLowerCase())) {
            throw new BusinessException(
                    "Invalid sortBy '" + sortBy + "' for instructor. Allowed: name, experience, status.");
        }

        return new ResponseEntity<>(
                academicService.filterByRole(role, name, status,
                        fieldOfInterest, enrolledCourse,
                        skill, experience, sortBy, sortDir),
                HttpStatus.OK);
    }
}
