package com.cts.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.cts.util.SecurityUtils;
import org.springframework.stereotype.Service;

import com.cts.annotation.AuditEvent;
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumerate.AcademicTerm;
import com.cts.enumerate.ExamStatus;
import com.cts.exception.*;
import com.cts.mapper.*;
import com.cts.repository.*;
import com.cts.service.ExamService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;
    private final RegistrarMapper registrarMapper;
    private final InstructorMapper instructorMapper;
    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final ExamCoordinatorRepository examCoordinatorRepository;

    private void verifyCoordinatorContext() {
        String loggedInEmail = SecurityUtils.getLoggedInEmail();
        examCoordinatorRepository.findByUser_Email(loggedInEmail)
                .orElseThrow(() -> new AccessDeniedException(
                        "Access Denied: Logged-in credentials do not belong to a valid Exam Coordinator profile."));
    }

    @Override
    @AuditEvent(eventName = "EXAM_CREATED", eventType = "CREATE", eventMessage = "A new exam was created in ACTIVE status")
    public ExamOutputDTO createExam(ExamInputDTO inputDTO) {
        verifyCoordinatorContext(); //

        Course course = courseRepository.findById(inputDTO.getCourseId()) //
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + inputDTO.getCourseId())); //

        Instructor instructor = instructorRepository.findById(inputDTO.getInstructorId()) //
                .orElseThrow(() -> new InstructorNotFoundException("Instructor not found with id: " + inputDTO.getInstructorId())); //

        // ── RULE 1: VALIDATE EXAM DATE FALLS WITHIN COURSE TIMELINE WINDOW ──
        if (inputDTO.getExamDate() != null) {
            LocalDate targetExamDate = inputDTO.getExamDate().toLocalDate();

            if (course.getStartDate() != null && targetExamDate.isBefore(course.getStartDate())) {
                throw new BusinessException("Scheduling Error: Exam date cannot be before the course start date (" + course.getStartDate() + ").");
            }
            if (course.getEndDate() != null && targetExamDate.isAfter(course.getEndDate())) {
                throw new BusinessException("Scheduling Error: Exam date cannot be after the course end date (" + course.getEndDate() + ").");
            }
        }

        // ── RULE 2: ENFORCE EXACTLY ONE FINAL EXAM PER COURSE TRACK ──
        if (examRepository.existsByCourse_CourseId(inputDTO.getCourseId())) { //
            throw new BusinessException("An exam has already been scheduled for this course (" + course.getTitle() + "). Only one exam is permitted per course."); //
        }

        // ── INSTRUCTOR CONFLICT DETECTION ──
        if (inputDTO.getExamDate() != null) {
            LocalDate targetExamDate = inputDTO.getExamDate().toLocalDate();
            List<Exam> instructorExams = examRepository.findByInstructor_InstructorId(inputDTO.getInstructorId());
            for (Exam existingExam : instructorExams) {
                if (existingExam.getExamDate() != null && existingExam.getExamDate().toLocalDate().isEqual(targetExamDate)) {
                    String instructorName = (instructor.getUser() != null) ? instructor.getUser().getName() : "Instructor";
                    throw new BusinessException("Conflict Detected: Instructor " + instructorName + " is already assigned to another exam on this date (" + targetExamDate + ").");
                }
            }
        }

        Exam exam = Exam.builder() //
                .title(inputDTO.getTitle()) //
                .description(inputDTO.getDescription()) //
                .term(inputDTO.getTerm()) //
                .examDate(inputDTO.getExamDate()) //
                .durationMinutes(inputDTO.getDurationMinutes()) //
                .totalMarks(100).passingMarks(40) //
                .status(ExamStatus.ACTIVE) //
                .course(course).instructor(instructor) //
                .createdAt(LocalDateTime.now()) //
                .build(); //

        return examMapper.toExamOutputDTO(examRepository.save(exam)); //
    }

    @Override
    @AuditEvent(eventName = "EXAMS_SEARCHED", eventType = "READ", eventMessage = "Exams were searched")
    public List<ExamOutputDTO> searchExams(Long courseId, Long instructorId, String term, String status) {
        ExamStatus statusFilter = parseStatus(status); //
        String termFilter = (term == null || term.isBlank()) ? null : term.trim().toUpperCase(); //
        return examRepository.searchExams(courseId, instructorId, termFilter, statusFilter) //
                .stream().map(examMapper::toExamOutputDTO).collect(Collectors.toList()); //
    }

    @Override
    @AuditEvent(eventName = "EXAM_FETCHED", eventType = "READ", eventMessage = "Exam details fetched by ID")
    public ExamOutputDTO getExamById(Long examId) {
        return examMapper.toExamOutputDTO(findExamOrThrow(examId)); //
    }

    @Override
    @AuditEvent(eventName = "EXAM_DELETED", eventType = "DELETE", eventMessage = "An exam was deleted")
    public void deleteExam(Long examId) {
        verifyCoordinatorContext(); //
        Exam exam = findExamOrThrow(examId); //
        long hoursUntilExam = java.time.temporal.ChronoUnit.HOURS.between(LocalDateTime.now(), exam.getExamDate()); //
        if (hoursUntilExam <= 24) { //
            throw new BusinessException("Exam cannot be deleted within 24 hours of the exam date. Hours remaining: " + hoursUntilExam); //
        }
        examRepository.delete(exam); //
    }

    @Override
    @AuditEvent(eventName = "EC_ALL_COURSES_FETCHED", eventType = "READ", eventMessage = "Exam coordinator fetched all courses")
    public List<RegistrarCourseResponseDTO> getAllCourses() {
        return courseRepository.findAll().stream() //
                .map(registrarMapper::toRegistrarCourseResponseDTO).collect(Collectors.toList()); //
    }

    @Override
    @AuditEvent(eventName = "EC_ALL_INSTRUCTORS_FETCHED", eventType = "READ", eventMessage = "Exam coordinator fetched all instructors")
    public List<InstructorOutputDTO> getAllInstructors() {
        return instructorRepository.findAll().stream() //
                .map(instructorMapper::toInstructorOutputDTO).collect(Collectors.toList()); //
    }

    private Exam findExamOrThrow(Long examId) {
        return examRepository.findById(examId) //
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + examId)); //
    }

    private ExamStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null; //
        try { return ExamStatus.valueOf(status.trim().toUpperCase()); } //
        catch (IllegalArgumentException ex) { //
            throw new BusinessException("Invalid status '" + status + "'. Allowed: ACTIVE, COMPLETED."); //
        }
    }
}