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

import com.cts.dto.ExamRoomInputDTO;
import com.cts.dto.ExamRoomOutputDTO;
import com.cts.entity.*;
import com.cts.enumerate.AcademicTerm;
import com.cts.enumerate.ExamStatus;
import com.cts.exception.BusinessException;
import com.cts.exception.ExamNotFoundException;
import com.cts.mapper.ExamRoomMapper;
import com.cts.repository.*;

public class ExamRoomServiceImplTest {

    @Mock private ExamRepository examRepository;
    @Mock private ExamRoomRepository examRoomRepository;
    @Mock private ExamRoomAllocationRepository allocationRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;
    @Mock private PhysicalRoomRepository physicalRoomRepository;
    @Mock private ExamRoomMapper examRoomMapper;

    @InjectMocks
    private ExamRoomServiceImpl examRoomService;

    private Course course;
    private Exam exam;
    private ExamRoomInputDTO inputDTO;
    private PhysicalRoom physicalRoom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        course = Course.builder().courseId(10L).title("Java FSE").build();

        exam = Exam.builder()
                .examId(1L).title("Java FSE Final")
                .course(course).status(ExamStatus.PUBLISHED)
                .term(AcademicTerm.SPRING_2026)
                .examDate(LocalDateTime.now().plusDays(2))
                .durationMinutes(120).build();

        inputDTO = ExamRoomInputDTO.builder()
                .physicalRoomId(5L).examId(1L).roomNumber(1).build();

        physicalRoom = PhysicalRoom.builder()
                .roomId(5L).roomName("Hall A").location("Block B").capacity(30).status("AVAILABLE").build();
    }

    @Test
    void createAndAllocate_success() {
        User u = User.builder().userId(1L).name("Ravi").build();
        Student s = Student.builder().studentId(1L).user(u).build();
        CourseEnrollment e = CourseEnrollment.builder().student(s).course(course).build();

        ExamRoom savedRoom = ExamRoom.builder().roomId(1L).roomName("Hall A").exam(exam).build();
        ExamRoomOutputDTO outputDTO = ExamRoomOutputDTO.builder().roomId(1L).roomName("Hall A").studentsAllocated(1).build();

        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(physicalRoomRepository.findById(5L)).thenReturn(Optional.of(physicalRoom));
        when(physicalRoomRepository.hasTimeOverlap(anyLong(), any(), any(), anyLong())).thenReturn(false);
        when(examRoomRepository.existsByExam_ExamIdAndRoomNumber(1L, 1)).thenReturn(false);
        when(enrollmentRepository.findByCourse_CourseId(10L)).thenReturn(Arrays.asList(e));
        when(allocationRepository.findAllocatedStudentIdsByExamId(1L)).thenReturn(Collections.emptyList());
        when(examRoomRepository.save(any(ExamRoom.class))).thenReturn(savedRoom);
        when(examRoomMapper.toOutputDTO(eq(savedRoom), anyList())).thenReturn(outputDTO);

        ExamRoomOutputDTO result = examRoomService.createAndAllocate(inputDTO);

        assertNotNull(result);
        assertEquals("Hall A", result.getRoomName());
        verify(physicalRoomRepository).save(physicalRoom);
    }
}