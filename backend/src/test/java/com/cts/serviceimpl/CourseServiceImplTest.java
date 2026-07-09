package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.exception.*;
import com.cts.mapper.*;
import com.cts.repository.*;
import com.cts.service.FileStorageService;
import com.cts.util.SecurityUtils;

public class CourseServiceImplTest {

    @Mock private CourseMapper courseMapper;
    @Mock private AssignmentMapper assignmentMapper;
    @Mock private CourseRepository courseRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private CourseMaterialFileRepository materialFileRepository;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentFileRepository assignmentFileRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private CourseEnrollmentRepository enrollmentRepository;
    @Mock private ExamResultRepository examResultRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private final String MOCK_EMAIL = "instructor@educore.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAssignedCourses_success() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Instructor mockInstructor = Instructor.builder()
                    .instructorId(1L)
                    .user(User.builder().email(MOCK_EMAIL).build())
                    .build();

            lenient().when(instructorRepository.findByUser_Email(anyString()))
                    .thenReturn(Optional.of(mockInstructor));
            lenient().when(courseRepository.findByInstructor_InstructorId(anyLong()))
                    .thenReturn(List.of(new Course()));

            List<CourseOutputDTO> result = courseService.getAssignedCourses();

            assertNotNull(result);
        }
    }

    @Test
    void getAssignedCourses_instructorNotFound() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            lenient().when(instructorRepository.findByUser_Email(anyString()))
                    .thenReturn(Optional.empty());

            assertThrows(InstructorNotFoundException.class, () -> courseService.getAssignedCourses());
        }
    }

    @Test
    void publishCourseMaterial_success() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Long mockCourseId = 10L;
            MultipartFile mockFile = mock(MultipartFile.class);
            lenient().when(mockFile.isEmpty()).thenReturn(false);

            Instructor mockInstructor = Instructor.builder()
                    .instructorId(1L)
                    .user(User.builder().email(MOCK_EMAIL).build())
                    .build();

            Course mockCourse = Course.builder()
                    .courseId(mockCourseId)
                    .title("Java")
                    .build();

            lenient().when(instructorRepository.findByUser_Email(anyString()))
                    .thenReturn(Optional.of(mockInstructor));
            lenient().when(courseRepository.findByCourseIdAndInstructor_InstructorId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(mockCourse));
            lenient().when(materialFileRepository.findByCourse_CourseId(anyLong()))
                    .thenReturn(Collections.emptyList());
            lenient().when(fileStorageService.generateMaterialFileName(anyString(), anyInt()))
                    .thenReturn("java_notes_1.pdf");
            lenient().when(fileStorageService.storeFile(any(), anyString(), anyString()))
                    .thenReturn("path/java_notes_1.pdf");
            
            CourseMaterialFile mockSaved = CourseMaterialFile.builder()
                    .fileId(100L)
                    .fileName("java_notes_1.pdf")
                    .build();
            lenient().when(materialFileRepository.save(any(CourseMaterialFile.class)))
                    .thenReturn(mockSaved);

            CourseMaterialFileOutputDTO mockDTO = CourseMaterialFileOutputDTO.builder()
                    .fileId(100L)
                    .fileName("java_notes_1.pdf")
                    .build();
            lenient().when(courseMapper.toCourseMaterialFileOutputDTO(any(CourseMaterialFile.class)))
                    .thenReturn(mockDTO);

            CourseMaterialFileOutputDTO result = courseService.publishCourseMaterial(mockCourseId, mockFile, "Supplementary Text");

            assertNotNull(result);
        }
    }

    @Test
    void publishCourseMaterial_emptyFileThrowsException() {
        try (var mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getLoggedInEmail).thenReturn(MOCK_EMAIL);

            Instructor mockInstructor = Instructor.builder()
                    .instructorId(1L)
                    .user(User.builder().email(MOCK_EMAIL).build())
                    .build();

            Course mockCourse = Course.builder()
                    .courseId(1L)
                    .title("Java")
                    .build();

            lenient().when(instructorRepository.findByUser_Email(anyString()))
                    .thenReturn(Optional.of(mockInstructor));
            lenient().when(courseRepository.findByCourseIdAndInstructor_InstructorId(anyLong(), anyLong()))
                    .thenReturn(Optional.of(mockCourse));

            MultipartFile mockFile = mock(MultipartFile.class);
            lenient().when(mockFile.isEmpty()).thenReturn(true);

            assertThrows(BusinessException.class, () -> courseService.publishCourseMaterial(1L, mockFile, "Text"));
        }
    }
}