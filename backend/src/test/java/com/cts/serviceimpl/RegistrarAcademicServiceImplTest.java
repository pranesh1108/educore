package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumerate.Role;
import com.cts.exception.*;
import com.cts.mapper.RegistrarMapper;
import com.cts.repository.*;
import com.cts.util.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class RegistrarAcademicServiceImplTest {

    @Mock private RegistrarMapper registrarMapper;
    @Mock private CourseRepository courseRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private CourseEnrollmentRepository enrollmentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private RegistrarRepository registrarRepository;

    @InjectMocks
    private RegistrarAcademicServiceImpl registrarAcademicService;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private User registrarUser;
    private Registrar registrar;
    private Instructor validInstructor;

    @BeforeEach
    void setUp() {
        // Mock static method for SecurityUtils logged-in context
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getLoggedInEmail).thenReturn("registrar@cts.com");

        registrarUser = User.builder()
                .userId(1L)
                .email("registrar@cts.com")
                .name("Admin Registrar")
                .role(Role.REGISTRAR)
                .build();

        registrar = Registrar.builder()
                .registrarId(10L)
                .user(registrarUser)
                .build();

        validInstructor = Instructor.builder()
                .instructorId(100L)
                .experience(5)
                .skills(List.of(com.cts.enumerate.InstructorSkill.JAVA)) // Adjust skill enum matching your entity
                .user(User.builder().userId(2L).name("John Doe").email("john@cts.com").build())
                .build();

        // Stub registrar context verification for tests
        lenient().when(registrarRepository.findByUserEmail("registrar@cts.com")).thenReturn(Optional.of(registrar));
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close(); // Clean up static mock after each test
    }

    // ==========================================
    // PROVISION NEW COURSE TESTS
    // ==========================================

    @Test
    @DisplayName("provisionNewCourse: Success without syllabus PDF")
    void provisionNewCourse_Success_WithoutSyllabus() {
        RegistrarCourseCreateDTO createDTO = RegistrarCourseCreateDTO.builder()
                .title("Spring Boot")
                .description("Learn Spring")
                .instructorId(100L)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(20))
                .enrollmentDeadlineDate(LocalDate.now().plusDays(2))
                .build();

        Course savedCourse = Course.builder().courseId(1L).title("Spring Boot").build();
        RegistrarCourseResponseDTO responseDTO = RegistrarCourseResponseDTO.builder().courseId(1L).title("Spring Boot").build();

        when(courseRepository.existsByTitleIgnoreCase("Spring Boot")).thenReturn(false);
        when(instructorRepository.findById(100L)).thenReturn(Optional.of(validInstructor));
        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);
        when(registrarMapper.toRegistrarCourseResponseDTO(savedCourse)).thenReturn(responseDTO);

        RegistrarCourseResponseDTO result = registrarAcademicService.provisionNewCourse(createDTO);

        assertNotNull(result);
        assertEquals(1L, result.getCourseId());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    @DisplayName("provisionNewCourse: Throws exception when Course title already exists")
    void provisionNewCourse_Throws_CourseAlreadyExists() {
        RegistrarCourseCreateDTO createDTO = RegistrarCourseCreateDTO.builder().title("Spring Boot").build();
        when(courseRepository.existsByTitleIgnoreCase("Spring Boot")).thenReturn(true);

        assertThrows(CourseAlreadyExistsException.class, () -> registrarAcademicService.provisionNewCourse(createDTO));
    }

    @Test
    @DisplayName("provisionNewCourse: Throws exception when Instructor not found")
    void provisionNewCourse_Throws_InstructorNotFound() {
        RegistrarCourseCreateDTO createDTO = RegistrarCourseCreateDTO.builder()
                .title("Spring Boot")
                .instructorId(999L)
                .build();

        when(courseRepository.existsByTitleIgnoreCase("Spring Boot")).thenReturn(false);
        when(instructorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(InstructorNotFoundException.class, () -> registrarAcademicService.provisionNewCourse(createDTO));
    }

    @Test
    @DisplayName("provisionNewCourse: Throws exception when Instructor profile incomplete")
    void provisionNewCourse_Throws_IncompleteInstructorProfile() {
        Instructor incompleteInstructor = Instructor.builder()
                .instructorId(101L)
                .skills(Collections.emptyList()) // No skills
                .experience(null)
                .build();

        RegistrarCourseCreateDTO createDTO = RegistrarCourseCreateDTO.builder()
                .title("Spring Boot")
                .instructorId(101L)
                .build();

        when(courseRepository.existsByTitleIgnoreCase("Spring Boot")).thenReturn(false);
        when(instructorRepository.findById(101L)).thenReturn(Optional.of(incompleteInstructor));

        assertThrows(BusinessException.class, () -> registrarAcademicService.provisionNewCourse(createDTO));
    }

    @Test
    @DisplayName("provisionNewCourse: Throws exception when StartDate is after EndDate")
    void provisionNewCourse_Throws_InvalidStartEndDateTimeline() {
        RegistrarCourseCreateDTO createDTO = RegistrarCourseCreateDTO.builder()
                .title("Spring Boot")
                .instructorId(100L)
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(5)) // Invalid: End before Start
                .enrollmentDeadlineDate(LocalDate.now().plusDays(2))
                .build();

        when(courseRepository.existsByTitleIgnoreCase("Spring Boot")).thenReturn(false);
        when(instructorRepository.findById(100L)).thenReturn(Optional.of(validInstructor));

        assertThrows(BusinessException.class, () -> registrarAcademicService.provisionNewCourse(createDTO));
    }

    @Test
    @DisplayName("provisionNewCourse: Throws exception when EnrollmentDeadline is after StartDate")
    void provisionNewCourse_Throws_InvalidEnrollmentDeadlineTimeline() {
        RegistrarCourseCreateDTO createDTO = RegistrarCourseCreateDTO.builder()
                .title("Spring Boot")
                .instructorId(100L)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(20))
                .enrollmentDeadlineDate(LocalDate.now().plusDays(8)) // Invalid: Deadline after start
                .build();

        when(courseRepository.existsByTitleIgnoreCase("Spring Boot")).thenReturn(false);
        when(instructorRepository.findById(100L)).thenReturn(Optional.of(validInstructor));

        assertThrows(BusinessException.class, () -> registrarAcademicService.provisionNewCourse(createDTO));
    }

    @Test
    @DisplayName("provisionNewCourse: Throws exception for invalid Base64 string in syllabus")
    void provisionNewCourse_Throws_InvalidBase64Syllabus() {
        RegistrarCourseCreateDTO createDTO = RegistrarCourseCreateDTO.builder()
                .title("Spring Boot")
                .instructorId(100L)
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(20))
                .enrollmentDeadlineDate(LocalDate.now().plusDays(2))
                .syllabusPath("INVALID_BASE_64_STRING_!!!")
                .build();

        when(courseRepository.existsByTitleIgnoreCase("Spring Boot")).thenReturn(false);
        when(instructorRepository.findById(100L)).thenReturn(Optional.of(validInstructor));

        assertThrows(BusinessException.class, () -> registrarAcademicService.provisionNewCourse(createDTO));
    }

    // ==========================================
    // GET ALL CONFIGURED COURSES TESTS
    // ==========================================

    @Test
    @DisplayName("getAllConfiguredCourses: Success fetching all courses")
    void getAllConfiguredCourses_Success() {
        Course c1 = Course.builder().courseId(1L).title("Course A").build();
        Course c2 = Course.builder().courseId(2L).title("Course B").build();

        when(courseRepository.findAll()).thenReturn(List.of(c1, c2));
        when(registrarMapper.toRegistrarCourseResponseDTO(any(Course.class)))
                .thenAnswer(invocation -> {
                    Course c = invocation.getArgument(0);
                    return RegistrarCourseResponseDTO.builder().courseId(c.getCourseId()).title(c.getTitle()).build();
                });

        List<RegistrarCourseResponseDTO> list = registrarAcademicService.getAllConfiguredCourses();

        assertEquals(2, list.size());
        assertEquals("Course A", list.get(0).getTitle());
    }

    // ==========================================
    // FILTER BY ROLE TESTS
    // ==========================================

    @Test
    @DisplayName("filterByRole: Filter students successfully")
    void filterByRole_Students_Success() {
        User studentUser = User.builder().userId(10L).name("Alice Smith").email("alice@test.com").build();
        Student student = Student.builder().studentId(100L).fieldOfInterest("Java").user(studentUser).build();

        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(enrollmentRepository.findByStudent_StudentId(100L)).thenReturn(Collections.emptyList());

        Object result = registrarAcademicService.filterByRole(
                "student", "Alice", null, "Java", null, null, null, "name", "asc"
        );

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<StudentFilterOutputDTO> list = (List<StudentFilterOutputDTO>) result;
        assertEquals(1, list.size());
        assertEquals("Alice Smith", list.get(0).getName());
    }

    @Test
    @DisplayName("filterByRole: Filter instructors successfully")
    void filterByRole_Instructors_Success() {
        when(instructorRepository.findAll()).thenReturn(List.of(validInstructor));
        when(courseRepository.findByInstructor_InstructorId(100L)).thenReturn(Collections.emptyList());

        Object result = registrarAcademicService.filterByRole(
                "instructor", "John", null, null, null, null, 2, "experience", "desc"
        );

        assertNotNull(result);
        assertTrue(result instanceof List);
        List<InstructorFilterOutputDTO> list = (List<InstructorFilterOutputDTO>) result;
        assertEquals(1, list.size());
        assertEquals("John Doe", list.get(0).getName());
    }

    @Test
    @DisplayName("filterByRole: Throws exception on invalid role")
    void filterByRole_Throws_InvalidRole() {
        assertThrows(BusinessException.class, () -> registrarAcademicService.filterByRole(
                "admin", null, null, null, null, null, null, "name", "asc"
        ));
    }

    @Test
    @DisplayName("verifyRegistrarContext: Throws exception when Registrar profile is missing")
    void verifyRegistrarContext_Throws_ResourceNotFound() {
        when(registrarRepository.findByUserEmail("registrar@cts.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> registrarAcademicService.getAllConfiguredCourses());
    }
}