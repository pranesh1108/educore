package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cts.dto.ExamResultOutputDTO;
import com.cts.service.ExamResultService;

public class ResultControllerTest {

    @Mock private ExamResultService examResultService;

    @InjectMocks
    private ResultController resultController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getResultsByStudent_success() {
        ExamResultOutputDTO dto = ExamResultOutputDTO.builder().resultId(1L).score(88.0).build();
        when(examResultService.getResultsByStudent()).thenReturn(List.of(dto));

        ResponseEntity<List<ExamResultOutputDTO>> response = resultController.getResultsByStudent();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(88.0, response.getBody().get(0).getScore());
    }
}