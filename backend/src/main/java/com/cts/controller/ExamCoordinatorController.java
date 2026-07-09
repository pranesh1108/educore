package com.cts.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cts.dto.*;
import com.cts.service.ExamResultService;
import com.cts.service.ExamRoomService;
import com.cts.service.ExamService;
import com.cts.service.PhysicalRoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/exam-coordinator")
@PreAuthorize("hasRole('EXAM_COORDINATOR')")
@Tag(name = "Exam Coordinator", description = "Exam lifecycle, room allocation, physical spaces, and context-secured results management")
public class ExamCoordinatorController {

    private final ExamService examService;
    private final ExamRoomService examRoomService;
    private final ExamResultService examResultService;
    private final PhysicalRoomService physicalRoomService;

    // ── EXAM LIFECYCLE MANAGEMENT ───────────────────────────────────────────

    @Operation(
            summary = "Create a new exam lifecycle track",
            description = "Creates a standard exam record initialized in ACTIVE status. Academic terms follow valid enum codes (e.g., SPRING_2026, FALL_2026). Expected Date Format: yyyy-MM-dd HH:mm"
    )
    @PostMapping("/exams")
    public ResponseEntity<ExamOutputDTO> createExam(@Valid @RequestBody ExamInputDTO inputDTO) {
        return new ResponseEntity<>(examService.createExam(inputDTO), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Search and filter exam records",
            description = "Queries the centralized exam records with optional filters like courseId, instructorId, specific academic term string blocks, or active statuses."
    )
    @GetMapping("/exams")
    public ResponseEntity<List<ExamOutputDTO>> searchExams(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long instructorId,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String status) {
        return new ResponseEntity<>(examService.searchExams(courseId, instructorId, term, status), HttpStatus.OK);
    }

    @Operation(
            summary = "Get exam record tracking details",
            description = "Retrieves structural scheduling setups, passing baseline configurations, and mirrored venue location fields by exam identifier."
    )
    @GetMapping("/exams/{examId}")
    public ResponseEntity<ExamOutputDTO> getExamDetails(@PathVariable Long examId) {
        return new ResponseEntity<>(examService.getExamById(examId), HttpStatus.OK);
    }

    @Operation(
            summary = "Delete an unexecuted exam track",
            description = "Removes an exam system configuration record completely. Restricts processing if execution milestones are less than 24 hours away."
    )
    @DeleteMapping("/exams/{examId}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long examId) {
        examService.deleteExam(examId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ── VENUE VENUE ALLOCATIONS AND ROOM BATCHING ────────────────────────────

    @Operation(
            summary = "Assign a physical venue slot and auto-allocate unassigned student batches",
            description = "Binds a standalone physical room record directly to an exam window. Automatically samples unallocated students registered to the parent course code and populates allocations up to defined limits."
    )
    @PostMapping("/exam-rooms")
    public ResponseEntity<ExamRoomOutputDTO> createAndAllocate(@Valid @RequestBody ExamRoomInputDTO inputDTO) {
        return new ResponseEntity<>(examRoomService.createAndAllocate(inputDTO), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all venue subdivisions registered to an exam",
            description = "Returns a structural breakdown listing every distinct room number block and matching student assignment records registered to an exam."
    )
    @GetMapping("/exam-rooms/exam/{examId}")
    public ResponseEntity<List<ExamRoomOutputDTO>> getRoomsForExam(@PathVariable Long examId) {
        return new ResponseEntity<>(examRoomService.getRoomsForExam(examId), HttpStatus.OK);
    }

    // ── UTILITY SYSTEM REFERENCE INTEGRATION ─────────────────────────────────

    @Operation(
            summary = "Get all registered instructors",
            description = "Returns a baseline list mapping active instructors alongside tracking attributes to facilitate lifecycle planning."
    )
    @GetMapping("/instructors")
    public ResponseEntity<List<InstructorOutputDTO>> getAllInstructors() {
        return new ResponseEntity<>(examService.getAllInstructors(), HttpStatus.OK);
    }

    // ── PHYSICAL ROOM UTILITIES ──────────────────────────────────────────────

    @Operation(
            summary = "Create a standalone physical room slot",
            description = "Creates an available room inside the inventory database with defined baseline thresholds (min capacity 1, max 200) prior to scheduling integration."
    )
    @PostMapping("/rooms")
    public ResponseEntity<PhysicalRoomOutputDTO> createRoom(@Valid @RequestBody PhysicalRoomInputDTO inputDTO) {
        return new ResponseEntity<>(physicalRoomService.createRoom(inputDTO), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all configured physical locations",
            description = "Lists all location properties tracked in the database, with optional filtering based on structural indicators (AVAILABLE or OCCUPIED)."
    )
    @GetMapping("/rooms")
    public ResponseEntity<List<PhysicalRoomOutputDTO>> getAllRooms(@RequestParam(required = false) String status) {
        return new ResponseEntity<>(physicalRoomService.getAllRooms(status), HttpStatus.OK);
    }

    // ── FINAL MARKS GRADING PUBLICATION ──────────────────────────────────────

    @Operation(
            summary = "Publish final examination results for a student tracking record",
            description = "Records a score, assigns a PASS indicator if the mark is 40.0 or higher, and transitions student profiles to a DROPPED tracking status to lock current active sessions."
    )
    @PostMapping("/results")
    public ResponseEntity<ExamResultOutputDTO> publishResult(@Valid @RequestBody ExamResultInputDTO inputDTO) {
        return new ResponseEntity<>(examResultService.publishResult(inputDTO), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get enrolled students for an exam",
            description = "Returns all students enrolled in the course linked to the given exam. Used to populate the student dropdown when publishing results."
    )
    @GetMapping("/exams/{examId}/enrolled-students")
    public ResponseEntity<List<ExamRoomAllocationStudentDTO>> getEnrolledStudentsForExam(@PathVariable Long examId) {
        return new ResponseEntity<>(examResultService.getEnrolledStudentsForExam(examId), HttpStatus.OK);
    }
}