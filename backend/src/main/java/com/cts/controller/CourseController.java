package com.cts.controller;

import com.cts.dto.RegistrarCourseResponseDTO;
import com.cts.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/courses")
@Tag(name = "Course Catalogue", description = "Shared course catalogue accessible by all authenticated roles")
@CrossOrigin(origins = "http://localhost:4200")
public class CourseController {

    private final StudentService studentService;

    @GetMapping("/all")
    public ResponseEntity<Page<RegistrarCourseResponseDTO>> getAllCourses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "title,asc") String sort) {

        String[] sortParams = sort.split(",");
        String sortProperty = sortParams[0];
        Sort.Direction sortDirection = (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Sort sorting = Sort.by(sortDirection, sortProperty);

        Pageable pageable;
        if (page != null && size != null) {
            pageable = PageRequest.of(page, size, sorting);
        } else {
            pageable = Pageable.unpaged(sorting);
        }

        return new ResponseEntity<>(studentService.filterCourses(title, topic, pageable), HttpStatus.OK);
    }

    @Operation(
            summary = "Stream Course Syllabus PDF Inline",
            description = "Streams the stored course syllabus PDF directly into the browser preview component container layout frame."
    )
    @GetMapping("/{courseId}/syllabus")
    public ResponseEntity<Resource> getSyllabusInline(@PathVariable Long courseId) {

        Resource resource = studentService.getSyllabusResource(courseId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}