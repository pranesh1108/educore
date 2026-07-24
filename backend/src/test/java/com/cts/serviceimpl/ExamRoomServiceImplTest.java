package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.cts.dto.ExamRoomInputDTO;
import com.cts.dto.ExamRoomOutputDTO;
import com.cts.entity.*;
import com.cts.mapper.ExamRoomMapper;
import com.cts.repository.*;
import com.cts.util.SecurityUtils;

public class ExamRoomServiceImplTest {

    @Mock private ExamRepository examRepository;
    @Mock private ExamRoomRepository examRoomRepository;
    @Mock private ExamRoomAllocationRepository allocationRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;
    @Mock private PhysicalRoomRepository physicalRoomRepository;
    @Mock private ExamRoomMapper examRoomMapper;
    @Mock private ExamCoordinatorRepository examCoordinatorRepository;

    @InjectMocks
    private ExamRoomServiceImpl examRoomService;

    private final String MOCK_EMAIL = "coordinator@educore.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAndAllocate_success() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            ExamCoordinator coordinator = ExamCoordinator.builder().coordinatorId(1L).build();            Course course = Course.builder().courseId(10L).title("Java").build();
            Exam exam = Exam.builder().examId(1L).course(course).examDate(LocalDateTime.now().plusDays(2)).durationMinutes(120).build();
            PhysicalRoom physicalRoom = PhysicalRoom.builder().roomId(5L).roomName("Hall A").capacity(30).build();

            User user = User.builder().userId(1L).name("Ravi").build();
            Student student = Student.builder().studentId(100L).user(user).build();
            CourseEnrollment enrollment = CourseEnrollment.builder().student(student).build();

            ExamRoom savedRoom = ExamRoom.builder().roomId(1L).roomName("Hall A").build();
            ExamRoomOutputDTO outputDTO = ExamRoomOutputDTO.builder().roomId(1L).roomName("Hall A").build();

            when(examCoordinatorRepository.findByUser_Email(MOCK_EMAIL)).thenReturn(Optional.of(coordinator));
            when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
            when(physicalRoomRepository.findById(5L)).thenReturn(Optional.of(physicalRoom));
            when(physicalRoomRepository.hasTimeOverlap(anyLong(), any(), any(), anyLong())).thenReturn(false);
            when(enrollmentRepository.findByCourse_CourseId(10L)).thenReturn(List.of(enrollment));
            when(allocationRepository.findAllocatedStudentIdsByExamId(1L)).thenReturn(Collections.emptyList());
            when(examRoomRepository.save(any(ExamRoom.class))).thenReturn(savedRoom);
            when(examRoomMapper.toOutputDTO(eq(savedRoom), anyList())).thenReturn(outputDTO);

            ExamRoomInputDTO inputDTO = ExamRoomInputDTO.builder().physicalRoomId(5L).examId(1L).build();
            ExamRoomOutputDTO result = examRoomService.createAndAllocate(inputDTO);

            assertNotNull(result);
            assertEquals("Hall A", result.getRoomName());
        }
    }
}