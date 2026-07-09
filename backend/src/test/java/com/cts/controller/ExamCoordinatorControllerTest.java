package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cts.dto.*;
import com.cts.enumerate.AcademicTerm;
import com.cts.exception.ExamNotFoundException;
import com.cts.service.ExamRoomService;
import com.cts.service.ExamService;
import com.cts.service.ExamResultService;
import com.cts.service.PhysicalRoomService;

public class ExamCoordinatorControllerTest {

    @Mock private ExamService examService;
    @Mock private ExamRoomService examRoomService;
    @Mock private ExamResultService examResultService;
    @Mock private PhysicalRoomService physicalRoomService;

    @InjectMocks
    private ExamCoordinatorController examCoordinatorController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private ExamInputDTO validExamInput() {
        return ExamInputDTO.builder()
                .courseId(1L).instructorId(1L)
                .title("Java FSE Final").term(AcademicTerm.SPRING_2026)
                .examDate(LocalDateTime.now().plusDays(5))
                .durationMinutes(120).build();
    }

    private ExamOutputDTO sampleExamOutput() {
        return ExamOutputDTO.builder()
                .examId(1L).title("Java FSE Final")
                .term(AcademicTerm.SPRING_2026).build();
    }

    @Test
    void createExam_success_returns201() {
        ExamInputDTO input = validExamInput();
        ExamOutputDTO output = sampleExamOutput();

        when(examService.createExam(any(ExamInputDTO.class))).thenReturn(output);

        ResponseEntity<ExamOutputDTO> response = examCoordinatorController.createExam(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Java FSE Final", response.getBody().getTitle());
    }

    @Test
    void createAndAllocate_success_returns201() {
        ExamRoomInputDTO input = ExamRoomInputDTO.builder().physicalRoomId(10L).examId(1L).roomNumber(1).build();
        ExamRoomOutputDTO output = ExamRoomOutputDTO.builder().roomId(1L).roomNumber(1).studentsAllocated(30).build();

        when(examRoomService.createAndAllocate(any(ExamRoomInputDTO.class))).thenReturn(output);

        ResponseEntity<ExamRoomOutputDTO> response = examCoordinatorController.createAndAllocate(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(30, response.getBody().getStudentsAllocated());
    }

    @Test
    void getRoomsForExam_success_returns200() {
        ExamRoomOutputDTO r1 = ExamRoomOutputDTO.builder().roomId(1L).roomName("Hall A").studentsAllocated(5).build();
        when(examRoomService.getRoomsForExam(1L)).thenReturn(Arrays.asList(r1));

        ResponseEntity<List<ExamRoomOutputDTO>> response = examCoordinatorController.getRoomsForExam(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}