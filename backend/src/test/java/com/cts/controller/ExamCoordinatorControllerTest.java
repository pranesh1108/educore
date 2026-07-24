package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cts.dto.*;
import com.cts.service.ExamResultService;
import com.cts.service.ExamRoomService;
import com.cts.service.ExamService;
import com.cts.service.PhysicalRoomService;

public class ExamCoordinatorControllerTest {

    @Mock private ExamService examService;
    @Mock private ExamRoomService examRoomService;
    @Mock private ExamResultService examResultService;
    @Mock private PhysicalRoomService physicalRoomService;

    @InjectMocks
    private ExamCoordinatorController examCoordinatorController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createExam_success() {
        ExamInputDTO input = ExamInputDTO.builder().courseId(1L).instructorId(1L).title("Java Exam").build();
        ExamOutputDTO output = ExamOutputDTO.builder().examId(1L).title("Java Exam").build();

        when(examService.createExam(any(ExamInputDTO.class))).thenReturn(output);

        ResponseEntity<ExamOutputDTO> response = examCoordinatorController.createExam(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Java Exam", response.getBody().getTitle());
    }

    @Test
    void createAndAllocate_success() {
        ExamRoomInputDTO input = ExamRoomInputDTO.builder().physicalRoomId(5L).examId(1L).build();
        ExamRoomOutputDTO output = ExamRoomOutputDTO.builder().roomId(1L).roomName("Hall A").build();

        when(examRoomService.createAndAllocate(any(ExamRoomInputDTO.class))).thenReturn(output);

        ResponseEntity<ExamRoomOutputDTO> response = examCoordinatorController.createAndAllocate(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Hall A", response.getBody().getRoomName());
    }

    @Test
    void publishResult_success() {
        ExamResultInputDTO input = ExamResultInputDTO.builder().examId(1L).studentId(1L).score(85.0).build();
        ExamResultOutputDTO output = ExamResultOutputDTO.builder().resultId(10L).score(85.0).build();

        when(examResultService.publishResult(any(ExamResultInputDTO.class))).thenReturn(output);

        ResponseEntity<ExamResultOutputDTO> response = examCoordinatorController.publishResult(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(85.0, response.getBody().getScore());
    }
}