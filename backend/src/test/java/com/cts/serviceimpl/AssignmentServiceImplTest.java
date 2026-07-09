package com.cts.serviceimpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.multipart.MultipartFile;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.exception.*;
import com.cts.mapper.AssignmentMapper;
import com.cts.repository.*;
import com.cts.service.FileStorageService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceImplTest {

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    @Mock
    private AssignmentMapper assignmentMapper;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private AssignmentFileRepository assignmentFileRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private InstructorRepository instructorRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private ExamResultRepository examResultRepository;
    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    private final String MOCK_EMAIL = "instructor@educore.com";

    @Test
    @WithMockUser(username = MOCK_EMAIL, roles = {"INSTRUCTOR"})
    public void publishAssignment_withoutFile_success() {
        // Arrange
        AssignmentInputDTO inputDTO = createMockInputDTO();
        Instructor mockInstructor = createMockInstructor();
        Course mockCourse = new Course();
        Assignment mockAssignment = new Assignment();

        // Security context lookup mocks
        when(instructorRepository.findByUser_Email(MOCK_EMAIL))
                .thenReturn(Optional.of(mockInstructor));
        when(enrollmentRepository.findByCourse_CourseId(inputDTO.getCourseId()))
                .thenReturn(Collections.emptyList());
        when(courseRepository.findByCourseIdAndInstructor_InstructorId(inputDTO.getCourseId(), mockInstructor.getInstructorId()))
                .thenReturn(Optional.of(mockCourse));
        when(instructorRepository.existsById(mockInstructor.getInstructorId()))
                .thenReturn(true);
        when(assignmentRepository.save(any(Assignment.class)))
                .thenReturn(mockAssignment);
        when(assignmentMapper.toAssignmentOutputDTO(eq(mockAssignment), anyList()))
                .thenReturn(new AssignmentOutputDTO());

        // Act - REMOVED the first argument (instructorId)
        AssignmentOutputDTO result = assignmentService.publishAssignment(inputDTO, null);

        // Assert
        assertNotNull(result);
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    @WithMockUser(username = MOCK_EMAIL, roles = {"INSTRUCTOR"})
    public void publishAssignment_withFile_savesAssignmentFile() {
        // Arrange
        AssignmentInputDTO inputDTO = createMockInputDTO();
        Instructor mockInstructor = createMockInstructor();
        Course mockCourse = Course.builder().courseId(1L).title("Java").build();
        Assignment mockAssignment = new Assignment();
        MultipartFile mockFile = mock(MultipartFile.class);

        when(mockFile.isEmpty()).thenReturn(false);
        when(instructorRepository.findByUser_Email(MOCK_EMAIL))
                .thenReturn(Optional.of(mockInstructor));
        when(enrollmentRepository.findByCourse_CourseId(inputDTO.getCourseId()))
                .thenReturn(Collections.emptyList());
        when(courseRepository.findByCourseIdAndInstructor_InstructorId(inputDTO.getCourseId(), mockInstructor.getInstructorId()))
                .thenReturn(Optional.of(mockCourse));
        when(instructorRepository.existsById(mockInstructor.getInstructorId()))
                .thenReturn(true);
        when(assignmentRepository.save(any(Assignment.class)))
                .thenReturn(mockAssignment);
        when(assignmentFileRepository.countByCourse(anyLong()))
                .thenReturn(0);
        when(fileStorageService.generateAssignmentFileName(anyString(), anyInt()))
                .thenReturn("java_assignment_1.pdf");
        when(fileStorageService.storeFile(any(), anyString(), anyString()))
                .thenReturn("uploads/assignments/java_assignment_1.pdf");
        when(assignmentFileRepository.save(any(AssignmentFile.class)))
                .thenReturn(new AssignmentFile());

        // Act - REMOVED the first argument (instructorId)
        AssignmentOutputDTO result = assignmentService.publishAssignment(inputDTO, mockFile);

        // Assert
        assertNotNull(result);
        verify(assignmentFileRepository).save(any(AssignmentFile.class));
    }

    @Test
    @WithMockUser(username = MOCK_EMAIL, roles = {"INSTRUCTOR"})
    public void publishAssignment_instructorNotFound_throwsException() {
        // Arrange
        AssignmentInputDTO inputDTO = createMockInputDTO();

        // Mocking token email resolution failure
        when(instructorRepository.findByUser_Email(MOCK_EMAIL))
                .thenReturn(Optional.empty());

        // Act & Assert - REMOVED the instructorId parameter from verification execution
        assertThrows(InstructorNotFoundException.class, () -> {
            assignmentService.publishAssignment(inputDTO, null);
        });
    }

    @Test
    @WithMockUser(username = MOCK_EMAIL, roles = {"INSTRUCTOR"})
    public void publishAssignment_courseNotOwned_throwsException() {
        // Arrange
        AssignmentInputDTO inputDTO = createMockInputDTO();
        Instructor mockInstructor = createMockInstructor();

        when(instructorRepository.findByUser_Email(MOCK_EMAIL))
                .thenReturn(Optional.of(mockInstructor));
        when(enrollmentRepository.findByCourse_CourseId(inputDTO.getCourseId()))
                .thenReturn(Collections.emptyList());
        when(instructorRepository.existsById(mockInstructor.getInstructorId()))
                .thenReturn(true);
        // Simulate course owning lookup failure
        when(courseRepository.findByCourseIdAndInstructor_InstructorId(inputDTO.getCourseId(), mockInstructor.getInstructorId()))
                .thenReturn(Optional.empty());

        // Act & Assert - REMOVED the instructorId parameter
        assertThrows(CourseNotAssignedToInstructorException.class, () -> {
            assignmentService.publishAssignment(inputDTO, null);
        });
    }

    // Helpers
    private AssignmentInputDTO createMockInputDTO() {
        return AssignmentInputDTO.builder()
                .courseId(1L)
                .title("OOP Assignment")
                .totalMarks(100.0)
                .dueDate(LocalDateTime.now().plusDays(5))
                .build();
    }

    private Instructor createMockInstructor() {
        return Instructor.builder()
                .instructorId(10L)
                .user(User.builder().email(MOCK_EMAIL).build())
                .build();
    }
}