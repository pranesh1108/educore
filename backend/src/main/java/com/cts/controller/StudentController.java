package com.cts.controller;

import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cts.dto.*;
import com.cts.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/student")
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student", description = "Student profile context-aware session operations")
public class StudentController {

    private final StudentService studentService;

    // ── PROFILE MANAGEMENT ───────────────────────────────────────────────────

    @Operation(summary = "Update student profile")
    @PutMapping("/profile/update")
    public ResponseEntity<StudentOutputDTO> updateStudentProfile(@Valid @RequestBody StudentInputDTO inputDTO) {
        return new ResponseEntity<>(studentService.updateStudentProfile(inputDTO), HttpStatus.OK);
    }

    @Operation(summary = "Get current student profile details")
    @GetMapping("/profile")
    public ResponseEntity<StudentOutputDTO> getStudentProfile() {
        return new ResponseEntity<>(studentService.getStudentProfile(), HttpStatus.OK);
    }

    // ── ENROLLMENT MANAGEMENT ────────────────────────────────────────────────

    @Operation(summary = "Enroll in a course")
    @PostMapping("/course/{courseId}/enroll")
    public ResponseEntity<EnrollmentOutputDTO> enrollInCourse(@PathVariable Long courseId) {
        return new ResponseEntity<>(studentService.enrollInCourse(courseId), HttpStatus.CREATED);
    }

    @Operation(summary = "Get my enrolled courses")
    @GetMapping("/my-courses")
    public ResponseEntity<List<EnrollmentOutputDTO>> getMyEnrolledCourses() {
        return new ResponseEntity<>(studentService.getMyEnrolledCourses(), HttpStatus.OK);
    }

    // ── UNIFIED CONTENT DELIVERY ─────────────────────────────────────────────

    @Operation(
            summary = "View course content (Materials & Assignments)",
            description = "Retrieves both published lecture material files and assignment criteria tasks for a course in a single network call. Access requires the calling student session to be actively enrolled in the target course."
    )
    @GetMapping("/course/{courseId}/content")
    public ResponseEntity<CourseContentResponseDTO> getCourseContent(@PathVariable Long courseId) {
        return new ResponseEntity<>(studentService.getCourseContent(courseId), HttpStatus.OK);
    }

    // ── RESOURCE DOWNLOADS ───────────────────────────────────────────────────

    @Operation(summary = "Download a course material file")
    @GetMapping("/material/{fileId}/download")
    public ResponseEntity<byte[]> downloadCourseMaterialFile(@PathVariable Long fileId) {
        byte[] bytes = studentService.downloadCourseMaterialFile(fileId);
        String fileName = studentService.getCourseMaterialFileName(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
    }

    @Operation(summary = "Download an assignment file")
    @GetMapping("/assignment-file/{fileId}/download")
    public ResponseEntity<byte[]> downloadAssignmentFile(@PathVariable Long fileId) {
        byte[] bytes = studentService.downloadAssignmentFile(fileId);
        String fileName = studentService.getAssignmentFileName(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
    }

    // ── SUBMISSIONS SYSTEM ───────────────────────────────────────────────────

    @Operation(summary = "Submit an assignment solution")
    @PostMapping(value = "/assignment/{assignmentId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionOutputDTO> submitAssignment(@PathVariable Long assignmentId, @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(studentService.submitAssignment(assignmentId, file), HttpStatus.CREATED);
    }

    @Operation(summary = "Get my submissions")
    @GetMapping("/my-submissions")
    public ResponseEntity<List<SubmissionOutputDTO>> getMySubmissions() {
        return new ResponseEntity<>(studentService.getMySubmissions(), HttpStatus.OK);
    }

    // ── ASSIGNED ASSESSMENT TRACKING ─────────────────────────────────────────

    @Operation(summary = "Get my upcoming exams")
    @GetMapping("/my-exams")
    public ResponseEntity<List<ExamOutputDTO>> getMyExams() {
        return new ResponseEntity<>(studentService.getMyExams(), HttpStatus.OK);
    }


}