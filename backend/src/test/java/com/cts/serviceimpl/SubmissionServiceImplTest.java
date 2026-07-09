package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cts.dto.GradeInputDTO;
import com.cts.dto.SubmissionOutputDTO;
import com.cts.entity.*;
import com.cts.enumerate.Role;
import com.cts.exception.*;
import com.cts.mapper.StudentMapper;
import com.cts.repository.*;
import com.cts.service.FileStorageService;

public class SubmissionServiceImplTest {

    @Mock private StudentMapper studentMapper;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private Instructor instructor;
    private Course course;
    private Submission submission;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User instructorUser = User.builder()
                .userId(1L)
                .name("Aditya")
                .email("instructor1@educore.com")
                .role(Role.INSTRUCTOR)
                .build();

        instructor = Instructor.builder()
                .instructorId(1L)
                .user(instructorUser)
                .build();

        User studentUser = User.builder()
                .userId(2L)
                .name("Ravi")
                .email("student1@educore.com")
                .role(Role.STUDENT)
                .build();

        // Removed .enrollmentNumber() to match production Student properties
        Student student = Student.builder()
                .studentId(1L)
                .user(studentUser)
                .build();

        course = Course.builder()
                .courseId(10L)
                .title("Java FSE")
                .instructor(instructor)
                .build();

        Assignment assignment = Assignment.builder()
                .assignmentId(100L)
                .title("Assignment 1")
                .course(course)
                .build();

        submission = Submission.builder()
                .submissionId(1L)
                .student(student)
                .assignment(assignment)
                .course(course)
                .filePath("uploads/submissions/file.pdf")
                .fileName("file.pdf")
                .submittedAt(LocalDateTime.now())
                .status("SUBMITTED")
                .build();
    }

    // ── GET SUBMISSIONS FOR COURSE ────────────────────────────────────

    @Test
    void getSubmissionsForCourse_success_returnsList() {
        SubmissionOutputDTO dto = SubmissionOutputDTO.builder()
                .submissionId(1L)
                .studentName("Ravi")
                .status("SUBMITTED")
                .build();

        when(instructorRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(submissionRepository.findByCourse_CourseId(10L))
                .thenReturn(Arrays.asList(submission));
        when(studentMapper.toSubmissionOutputDTO(submission)).thenReturn(dto);

        List<SubmissionOutputDTO> result =
                submissionService.getSubmissionsForCourse(1L, 10L);

        assertEquals(1, result.size());
        assertEquals("Ravi", result.get(0).getStudentName());
        verify(submissionRepository).findByCourse_CourseId(10L);
    }

    @Test
    void getSubmissionsForCourse_noSubmissions_throwsException() {
        when(instructorRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(submissionRepository.findByCourse_CourseId(10L))
                .thenReturn(Collections.emptyList());

        assertThrows(SubmissionNotFoundException.class,
                () -> submissionService.getSubmissionsForCourse(1L, 10L));
    }

    @Test
    void getSubmissionsForCourse_instructorNotFound_throwsException() {
        when(instructorRepository.existsById(99L)).thenReturn(false);

        assertThrows(InstructorNotFoundException.class,
                () -> submissionService.getSubmissionsForCourse(99L, 10L));
    }

    @Test
    void getSubmissionsForCourse_courseNotOwned_throwsException() {
        Instructor other = Instructor.builder().instructorId(99L).build();
        Course otherCourse = Course.builder()
                .courseId(10L)
                .instructor(other)
                .build();

        when(instructorRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findById(10L)).thenReturn(Optional.of(otherCourse));

        assertThrows(CourseNotAssignedToInstructorException.class,
                () -> submissionService.getSubmissionsForCourse(1L, 10L));
    }

    // ── GRADE SUBMISSION ──────────────────────────────────────────────

    @Test
    void gradeSubmission_success_statusBecomesGraded() {
        GradeInputDTO gradeInput = GradeInputDTO.builder()
                .grade(85.0)
                .feedback("Good work")
                .build();

        SubmissionOutputDTO dto = SubmissionOutputDTO.builder()
                .submissionId(1L)
                .grade(85.0)
                .status("GRADED")
                .build();

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(instructorRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);
        when(studentMapper.toSubmissionOutputDTO(submission)).thenReturn(dto);

        SubmissionOutputDTO result =
                submissionService.gradeSubmission(1L, 1L, gradeInput);

        assertNotNull(result);
        assertEquals("GRADED", submission.getStatus());
        assertEquals(85.0, submission.getGrade());
        assertEquals("Good work", submission.getFeedback());
        assertNotNull(submission.getGradedAt());
    }

    @Test
    void gradeSubmission_gradeAbove100_throwsException() {
        GradeInputDTO gradeInput = GradeInputDTO.builder().grade(101.0).build();

        assertThrows(InvalidGradeException.class,
                () -> submissionService.gradeSubmission(1L, 1L, gradeInput));
    }

    @Test
    void gradeSubmission_gradeBelow0_throwsException() {
        GradeInputDTO gradeInput = GradeInputDTO.builder().grade(-1.0).build();

        assertThrows(InvalidGradeException.class,
                () -> submissionService.gradeSubmission(1L, 1L, gradeInput));
    }

    @Test
    void gradeSubmission_nullGrade_throwsException() {
        GradeInputDTO gradeInput = GradeInputDTO.builder().grade(null).build();

        assertThrows(InvalidGradeException.class,
                () -> submissionService.gradeSubmission(1L, 1L, gradeInput));
    }

    @Test
    void gradeSubmission_submissionNotFound_throwsException() {
        GradeInputDTO gradeInput = GradeInputDTO.builder().grade(80.0).build();
        when(submissionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(SubmissionNotFoundException.class,
                () -> submissionService.gradeSubmission(1L, 999L, gradeInput));
    }

    // ── DOWNLOAD SUBMISSION FILE ──────────────────────────────────────

    @Test
    void downloadSubmissionFile_success_returnsByteArray() {
        byte[] expected = "pdf content".getBytes();

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(instructorRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(fileStorageService.loadFile("uploads/submissions/file.pdf"))
                .thenReturn(expected);

        byte[] result = submissionService.downloadSubmissionFile(1L, 1L);

        assertArrayEquals(expected, result);
    }

    @Test
    void downloadSubmissionFile_noFilePath_throwsInvalidFileException() {
        submission.setFilePath(null);

        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(instructorRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThrows(InvalidFileException.class,
                () -> submissionService.downloadSubmissionFile(1L, 1L));
    }
}