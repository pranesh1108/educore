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

import com.cts.dto.ExamInputDTO;
import com.cts.dto.ExamOutputDTO;
import com.cts.entity.*;
import com.cts.enumerate.AcademicTerm;
import com.cts.enumerate.ExamStatus;
import com.cts.exception.*;
import com.cts.mapper.ExamMapper;
import com.cts.repository.*;

public class ExamServiceImplTest {

    @Mock private ExamMapper examMapper;
    @Mock private ExamRepository examRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private InstructorRepository instructorRepository;

    @InjectMocks
    private ExamServiceImpl examService;

    private Course course;
    private Instructor instructor;
    private Exam exam;
    private ExamInputDTO inputDTO;
    private ExamOutputDTO outputDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = User.builder().userId(1L).name("Aditya").email("instructor1@educore.com").build();
        instructor = Instructor.builder().instructorId(1L).user(user).status("ACTIVE").build();
        course = Course.builder().courseId(10L).title("Java FSE").build();

        exam = Exam.builder()
                .examId(1L).title("Java FSE Final")
                .term(AcademicTerm.FALL_2026).status(ExamStatus.DRAFT)
                .course(course).instructor(instructor)
                .examDate(LocalDateTime.now().plusDays(5))
                .durationMinutes(120).totalMarks(100).passingMarks(40)
                .createdAt(LocalDateTime.now()).build();

        inputDTO = ExamInputDTO.builder()
                .courseId(10L).instructorId(1L)
                .title("Java FSE Final").description("Final exam")
                .term(AcademicTerm.FALL_2026)
                .examDate(LocalDateTime.now().plusDays(5))
                .durationMinutes(120)
                .build();

        outputDTO = ExamOutputDTO.builder()
                .examId(1L).title("Java FSE Final")
                .term(AcademicTerm.FALL_2026)
                .courseId(10L).courseTitle("Java FSE")
                .instructorId(1L).build();
    }

    @Test
    void createExam_success_statusIsDraft() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(examRepository.save(any(Exam.class))).thenReturn(exam);
        when(examMapper.toExamOutputDTO(exam)).thenReturn(outputDTO);

        ExamOutputDTO result = examService.createExam(inputDTO);

        assertNotNull(result);
        assertEquals("Java FSE Final", result.getTitle());
        verify(examRepository).save(any(Exam.class));
    }
}