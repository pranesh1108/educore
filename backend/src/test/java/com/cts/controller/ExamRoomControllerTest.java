package com.cts.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cts.dto.ExamRoomAllocationStudentDTO;
import com.cts.dto.ExamRoomInputDTO;
import com.cts.dto.ExamRoomOutputDTO;
import com.cts.enumerate.AcademicTerm;
import com.cts.exception.BusinessException;
import com.cts.exception.ExamNotFoundException;
import com.cts.service.ExamRoomService;
import com.cts.service.ExamService;
import com.cts.service.ExamResultService;
import com.cts.service.PhysicalRoomService;

public class ExamRoomControllerTest {

    @Mock
    private ExamRoomService examRoomService;

    // Keep unused service mocks to allow ExamCoordinatorController to initialize correctly via Mockito
    @Mock private ExamService examService;
    @Mock private ExamResultService examResultService;
    @Mock private PhysicalRoomService physicalRoomService;

    // Injecting into ExamCoordinatorController since ExamRoomController does not exist in production
    @InjectMocks
    private ExamCoordinatorController examCoordinatorController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Helper — valid input matching production ExamRoomInputDTO fields
    private ExamRoomInputDTO validInput() {
        return ExamRoomInputDTO.builder()
                .physicalRoomId(10L)
                .examId(1L)
                .roomNumber(1)
                .build();
    }

    // Helper — sample output using type-safe AcademicTerm enum and production fields
    private ExamRoomOutputDTO sampleOutput(int studentCount) {
        List<ExamRoomAllocationStudentDTO> students = new java.util.ArrayList<>();
        for (int i = 1; i <= studentCount; i++) {
            students.add(ExamRoomAllocationStudentDTO.builder()
                    .allocationId((long) i)
                    .studentId((long) i)
                    .studentName("Student " + i)
                    .email("student" + i + "@educore360.com")
                    .build());
        }
        return ExamRoomOutputDTO.builder()
                .roomId(1L)
                .roomName("Hall A")
                .location("Block B, Floor 2")
                .capacity(30)
                .roomNumber(1)
                .examId(1L)
                .examTitle("Java FSE Final Exam")
                .term(AcademicTerm.FALL_2026)
                .examDate(LocalDateTime.now().plusDays(2))
                .studentsAllocated(studentCount)
                .students(students)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── CREATE ROOM + ASSIGN + ALLOCATE ───────────────────────────────

    @Test
    void createAndAllocate_success_returns201() {
        ExamRoomInputDTO input = validInput();
        ExamRoomOutputDTO output = sampleOutput(30);

        when(examRoomService.createAndAllocate(any(ExamRoomInputDTO.class))).thenReturn(output);

        ResponseEntity<ExamRoomOutputDTO> response =
                examCoordinatorController.createAndAllocate(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getRoomId());
        assertEquals("Hall A", response.getBody().getRoomName());
        assertEquals("Block B, Floor 2", response.getBody().getLocation());
        assertEquals(30, response.getBody().getCapacity());
        assertEquals(1, response.getBody().getRoomNumber());
        assertEquals(30, response.getBody().getStudentsAllocated());
        assertEquals(30, response.getBody().getStudents().size());
        verify(examRoomService).createAndAllocate(input);
    }

    @Test
    void createAndAllocate_partialRoom_returns201WithFewerStudents() {
        ExamRoomInputDTO input = ExamRoomInputDTO.builder()
                .physicalRoomId(11L)
                .examId(1L)
                .roomNumber(2)
                .build();

        ExamRoomOutputDTO output = sampleOutput(10);
        output.setRoomName("Hall B");
        output.setRoomNumber(2);
        output.setStudentsAllocated(10);

        when(examRoomService.createAndAllocate(any(ExamRoomInputDTO.class))).thenReturn(output);

        ResponseEntity<ExamRoomOutputDTO> response =
                examCoordinatorController.createAndAllocate(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(10, response.getBody().getStudentsAllocated());
        verify(examRoomService).createAndAllocate(input);
    }

    @Test
    void createAndAllocate_examNotFound_throwsException() {
        ExamRoomInputDTO input = ExamRoomInputDTO.builder()
                .physicalRoomId(10L)
                .examId(99L)
                .roomNumber(1)
                .build();

        when(examRoomService.createAndAllocate(any(ExamRoomInputDTO.class)))
                .thenThrow(new ExamNotFoundException("Exam not found with id: 99"));

        ExamNotFoundException ex = assertThrows(ExamNotFoundException.class,
                () -> examCoordinatorController.createAndAllocate(input));

        assertTrue(ex.getMessage().contains("99"));
        verify(examRoomService).createAndAllocate(input);
    }

    @Test
    void createAndAllocate_roomNumberAlreadyUsed_throwsException() {
        ExamRoomInputDTO input = validInput();

        when(examRoomService.createAndAllocate(any(ExamRoomInputDTO.class)))
                .thenThrow(new BusinessException("Room number 1 is already created for exam id: 1."));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> examCoordinatorController.createAndAllocate(input));

        assertTrue(ex.getMessage().contains("Room number 1"));
        verify(examRoomService).createAndAllocate(input);
    }

    @Test
    void createAndAllocate_roomConflict_throwsException() {
        ExamRoomInputDTO input = validInput();

        when(examRoomService.createAndAllocate(any(ExamRoomInputDTO.class)))
                .thenThrow(new BusinessException("Room 'Hall A' is already assigned to another exam at the same date and time."));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> examCoordinatorController.createAndAllocate(input));

        assertTrue(ex.getMessage().contains("Hall A"));
        verify(examRoomService).createAndAllocate(input);
    }

    @Test
    void createAndAllocate_noStudentsEnrolled_throwsException() {
        ExamRoomInputDTO input = validInput();

        when(examRoomService.createAndAllocate(any(ExamRoomInputDTO.class)))
                .thenThrow(new BusinessException("No students are enrolled in the course for this exam."));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> examCoordinatorController.createAndAllocate(input));

        assertTrue(ex.getMessage().contains("No students"));
        verify(examRoomService).createAndAllocate(input);
    }

    @Test
    void createAndAllocate_allStudentsAlreadyAllocated_throwsException() {
        ExamRoomInputDTO input = ExamRoomInputDTO.builder()
                .physicalRoomId(12L)
                .examId(1L)
                .roomNumber(3)
                .build();

        when(examRoomService.createAndAllocate(any(ExamRoomInputDTO.class)))
                .thenThrow(new BusinessException("All 40 enrolled students have already been allocated."));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> examCoordinatorController.createAndAllocate(input));

        assertTrue(ex.getMessage().contains("already been allocated"));
        verify(examRoomService).createAndAllocate(input);
    }

    // ── GET ROOMS FOR EXAM ────────────────────────────────────────────

    @Test
    void getRoomsForExam_success_returns200() {
        ExamRoomOutputDTO room1 = sampleOutput(30);
        ExamRoomOutputDTO room2 = sampleOutput(10);
        room2.setRoomId(2L);
        room2.setRoomName("Hall B");
        room2.setRoomNumber(2);
        room2.setStudentsAllocated(10);

        when(examRoomService.getRoomsForExam(1L))
                .thenReturn(Arrays.asList(room1, room2));

        ResponseEntity<List<ExamRoomOutputDTO>> response =
                examCoordinatorController.getRoomsForExam(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Hall A", response.getBody().get(0).getRoomName());
        assertEquals("Hall B", response.getBody().get(1).getRoomName());
        assertEquals(30, response.getBody().get(0).getStudentsAllocated());
        assertEquals(10, response.getBody().get(1).getStudentsAllocated());
        verify(examRoomService).getRoomsForExam(1L);
    }

    @Test
    void getRoomsForExam_noRoomsYet_returnsEmptyList() {
        when(examRoomService.getRoomsForExam(1L))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<ExamRoomOutputDTO>> response =
                examCoordinatorController.getRoomsForExam(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(examRoomService).getRoomsForExam(1L);
    }

    @Test
    void getRoomsForExam_examNotFound_throwsException() {
        when(examRoomService.getRoomsForExam(99L))
                .thenThrow(new ExamNotFoundException("Exam not found with id: 99"));

        ExamNotFoundException ex = assertThrows(ExamNotFoundException.class,
                () -> examCoordinatorController.getRoomsForExam(99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(examRoomService).getRoomsForExam(99L);
    }

    @Test
    void getRoomsForExam_singleRoom_returnsCorrectStudentList() {
        ExamRoomOutputDTO room = sampleOutput(5);

        when(examRoomService.getRoomsForExam(1L))
                .thenReturn(Arrays.asList(room));

        ResponseEntity<List<ExamRoomOutputDTO>> response =
                examCoordinatorController.getRoomsForExam(1L);

        assertEquals(1, response.getBody().size());
        assertEquals(5, response.getBody().get(0).getStudentsAllocated());
        assertEquals(5, response.getBody().get(0).getStudents().size());
        assertEquals("Student 1",
                response.getBody().get(0).getStudents().get(0).getStudentName());

        // FIXED: Changed domain to match mock data helper setup exactly
        assertEquals("student1@educore360.com",
                response.getBody().get(0).getStudents().get(0).getEmail());
    }
}