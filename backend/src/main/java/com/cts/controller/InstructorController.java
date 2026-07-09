package com.cts.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cts.dto.*;
import com.cts.service.AssignmentService;
import com.cts.service.CourseService;
import com.cts.service.InstructorService;
import com.cts.service.SubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/instructor")
@PreAuthorize("hasRole('INSTRUCTOR')")
@Tag(name = "Instructor", description = "Instructor profile, course materials, assignments, grading, and enrolled student endpoints derived securely via token context")
public class InstructorController {

    private final InstructorService instructorService;
    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final Validator validator;

    // ── PROFILE MANAGEMENT ───────────────────────────────────────────────────

    @Operation(
            summary = "Update instructor profile",
            description = "Updates skills alignment, professional experience metrics, and date of birth details using the active token identity session context."
    )
    @PutMapping("/profile/update")
    public ResponseEntity<InstructorOutputDTO> updateInstructorProfile(
            @Valid @RequestBody InstructorInputDTO inputDTO) {
        return new ResponseEntity<>(
                instructorService.updateInstructorProfile(inputDTO), HttpStatus.OK);
    }

    @Operation(
            summary = "Get current instructor profile details",
            description = "Returns profile record specifications and tracking attributes for the currently logged-in session account context."
    )
    @GetMapping("/profile")
    public ResponseEntity<InstructorOutputDTO> getInstructorProfile() {
        return new ResponseEntity<>(instructorService.getInstructorProfile(), HttpStatus.OK);
    }

    // ── COURSE MANAGEMENT ────────────────────────────────────────────────────

    @Operation(
            summary = "Get my assigned courses",
            description = "Retrieves all active configured instruction tracks assigned to the calling token identity profile."
    )
    @GetMapping("/my-courses")
    public ResponseEntity<List<CourseOutputDTO>> getAssignedCourses() {
        return new ResponseEntity<>(courseService.getAssignedCourses(), HttpStatus.OK);
    }

    @Operation(
            summary = "Get enrolled students for a course",
            description = "Gathers explicit course registration tracking logs and associated student information records linked to an assigned course track."
    )
    @GetMapping("/course/{courseId}/enrolled-students")
    public ResponseEntity<List<EnrollmentOutputDTO>> getEnrolledStudents(
            @PathVariable Long courseId) {
        return new ResponseEntity<>(instructorService.getEnrolledStudents(courseId), HttpStatus.OK);
    }

    // ── COURSE MATERIALS RESOURCES ───────────────────────────────────────────

    @Operation(
            summary = "Publish course material handout",
            description = "Publishes mandatory PDF content handouts accompanied by optional description text guidelines."
    )
    @PostMapping(value = "/course/{courseId}/material", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseMaterialFileOutputDTO> publishCourseMaterial(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String textContent) {
        return new ResponseEntity<>(
                courseService.publishCourseMaterial(courseId, file, textContent), HttpStatus.OK);
    }

    // Replace the old file retrieval endpoints inside InstructorController.java with this method:

    @Operation(
            summary = "View course resources (Materials & Assignment Files)",
            description = "Retrieves both published lecture handouts and evaluation template files for a course in a single combined call. Access is restricted strictly to the instructor assigned to the course."
    )
    @GetMapping("/course/{courseId}/resources")
    public ResponseEntity<InstructorResourceResponseDTO> getCourseResources(@PathVariable Long courseId) {
        return new ResponseEntity<>(courseService.getCourseResources(courseId), HttpStatus.OK);
    }

    // ── ASSIGNMENTS MANAGEMENT ───────────────────────────────────────────────

    @Operation(
            summary = "Publish a course assignment",
            description = "Publishes a new evaluation task track. Expects multipart form parameters: courseId, title, totalMarks, instructions (optional), and a guidelines template PDF file."
    )
    @PostMapping(value = "/assignment/publish", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssignmentOutputDTO> publishAssignment(
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam(required = false) String instructions,
            @RequestParam Double totalMarks,
            @RequestParam String dueDate,
            @RequestParam MultipartFile file) {

        java.time.LocalDateTime parsedDueDate = java.time.LocalDateTime.parse(
                dueDate, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        AssignmentInputDTO assignmentInputDTO = AssignmentInputDTO.builder()
                .courseId(courseId)
                .title(title)
                .instructions(instructions)
                .totalMarks(totalMarks)
                .dueDate(parsedDueDate)
                .build();

        Set<ConstraintViolation<AssignmentInputDTO>> violations = validator.validate(assignmentInputDTO);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ConstraintViolationException(message, violations);
        }

        return new ResponseEntity<>(
                assignmentService.publishAssignment(assignmentInputDTO, file), HttpStatus.CREATED);
    }



    // ── SUBMISSIONS AND EVALUATION ───────────────────────────────────────────

    @Operation(
            summary = "View student submissions for a course",
            description = "Retrieves all completed assignment tasks uploaded by registered student profiles inside an assigned academic context track."
    )
    @GetMapping("/course/{courseId}/submissions")
    public ResponseEntity<List<SubmissionOutputDTO>> getSubmissions(
            @PathVariable Long courseId) {
        return new ResponseEntity<>(submissionService.getSubmissionsForCourse(courseId), HttpStatus.OK);
    }

    @Operation(
            summary = "Download a student submission file",
            description = "Downloads a student solution sheet submission document stream as an attachment payload directly in binary PDF form."
    )
    @GetMapping("/submission/{submissionId}/download")
    public ResponseEntity<byte[]> downloadSubmissionFile(
            @PathVariable Long submissionId) {

        byte[] fileBytes = submissionService.downloadSubmissionFile(submissionId);
        String fileName  = submissionService.getSubmissionFileName(submissionId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(fileBytes);
    }

    @Operation(
            summary = "Grade a student submission",
            description = "Applies a definitive numerical grade rating (0.0 to 100.0) alongside supplementary context commentary feedback text parameters to a student submission track record."
    )
    @PutMapping("/submission/{submissionId}/grade")
    public ResponseEntity<SubmissionOutputDTO> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeInputDTO gradeInputDTO) {
        return new ResponseEntity<>(
                submissionService.gradeSubmission(submissionId, gradeInputDTO), HttpStatus.OK);
    }

    @Operation(
            summary = "Get my assigned exam terms",
            description = "Lists scheduling data logs concerning formal examination tracks directly matched to the calling session context identity profile."
    )
    @GetMapping("/my-exams")
    public ResponseEntity<List<ExamOutputDTO>> getMyExams() {
        return new ResponseEntity<>(instructorService.getMyExams(), HttpStatus.OK);
    }


}