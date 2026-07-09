package com.cts.controller;

import com.cts.dto.RegistrarCourseResponseDTO;
import com.cts.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/courses")
@Tag(name = "Course Catalogue", description = "Shared course catalogue accessible by all authenticated roles")
@CrossOrigin(origins = "http://localhost:4200") // Allows frontend to render embedded assets safely
public class CourseController {

    private final StudentService studentService;

    // Define storage path root mapping
    private final Path rootLocation = Paths.get("uploads/syllabi");

    @Operation(
            summary = "Get / filter course catalogue (Optional Pagination)",
            description = "Returns the course catalogue either fully or in paginated blocks. Pass 'page' and 'size' to activate pagination. If omitted, all matching courses are returned at once."
    )
    @GetMapping("/all")
    public ResponseEntity<Page<RegistrarCourseResponseDTO>> getAllCourses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String topic,
            @Parameter(description = "Zero-based page index (0..N). Omit along with 'size' to fetch an unpaged result list.")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "The number of course elements to return on a single page frame block.")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "Sorting configuration criteria tracking in string layout format: fieldName,(asc|desc). Default sorting targets 'title,asc'.")
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

    // ── ADDED: Streaming Resource Endpoint ──
    @Operation(
            summary = "Stream Course Syllabus PDF Inline",
            description = "Streams the stored course syllabus PDF directly into the browser preview component container layout frame."
    )
    @GetMapping("/{courseId}/syllabus")
    public ResponseEntity<Resource> getSyllabusInline(@PathVariable Long courseId) {
        try {
            // Fetch the course from the catalog to get the recorded filename string
            RegistrarCourseResponseDTO courseDto = studentService.getAllCourses().stream()
                    .filter(c -> c.getCourseId().equals(courseId))
                    .findFirst()
                    .orElse(null);

            if (courseDto == null || courseDto.getSyllabusPath() == null || courseDto.getSyllabusPath().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            // Resolve file from storage directory path locations
            Path file = rootLocation.resolve(courseDto.getSyllabusPath()).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    contentType = "application/pdf";
                }

                return ResponseEntity.ok()
                        // "inline" disposition allows the browser to render it directly inside the viewer template component layout frame
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}