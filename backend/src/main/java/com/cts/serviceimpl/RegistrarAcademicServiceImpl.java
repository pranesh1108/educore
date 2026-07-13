package com.cts.serviceimpl;

import com.cts.util.SecurityUtils;
import com.cts.annotation.AuditEvent;
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.exception.*;
import com.cts.mapper.RegistrarMapper;
import com.cts.repository.*;
import com.cts.service.RegistrarAcademicService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RegistrarAcademicServiceImpl implements RegistrarAcademicService {

    private final RegistrarMapper registrarMapper;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final RegistrarRepository registrarRepository;

    private final Path rootLocation = Paths.get("uploads/syllabi");

    private void verifyRegistrarContext() {
        String loggedInEmail = SecurityUtils.getLoggedInEmail();
        registrarRepository.findByUserEmail(loggedInEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registrar profile not found for logged-in credentials: " + loggedInEmail));
    }


    @Override
    @Transactional
    @AuditEvent(eventName = "COURSE_PROVISIONED", eventType = "CREATE", eventMessage = "A new course was provisioned and assigned to an instructor with a decoded syllabus PDF")
    public RegistrarCourseResponseDTO provisionNewCourse(RegistrarCourseCreateDTO createDTO) {
        verifyRegistrarContext();

        if (courseRepository.existsByTitleIgnoreCase(createDTO.getTitle())) {
            throw new CourseAlreadyExistsException("A course titled '" + createDTO.getTitle() + "' already exists.");
        }

        Instructor instructor = instructorRepository.findById(createDTO.getInstructorId())
                .orElseThrow(() -> new InstructorNotFoundException("Instructor not found with id: " + createDTO.getInstructorId()));

        if (instructor.getSkills() == null || instructor.getSkills().isEmpty() || instructor.getExperience() == null) {
            throw new BusinessException("Instructor has not updated their profile completely.");
        }

        if (createDTO.getStartDate().isAfter(createDTO.getEndDate())) {
            throw new BusinessException("Invalid Timeline: Course start date cannot occur after the course end date.");
        }
        if (createDTO.getEnrollmentDeadlineDate().isAfter(createDTO.getStartDate())) {
            throw new BusinessException("Invalid Timeline: Enrollment deadline date cannot occur after the course start date.");
        }

        Course course = Course.builder()
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .prerequisite(createDTO.getPrerequisite())
                .startDate(createDTO.getStartDate())
                .endDate(createDTO.getEndDate())
                .enrollmentDeadlineDate(createDTO.getEnrollmentDeadlineDate())
                .instructor(instructor)
                .build();

        // 2. ── CHANGED: Read and process the Base64 string payload ──
        // (Assuming you added `private String syllabusPath;` inside RegistrarCourseCreateDTO to hold the base64 text)
        if (createDTO.getSyllabusPath() != null && !createDTO.getSyllabusPath().isBlank()) {
            try {
                // Ensure the folder location structures exist safely
                if (!Files.exists(rootLocation)) {
                    Files.createDirectories(rootLocation);
                }

                // Decode the base64 string data payload directly back into a binary byte array
                byte[] pdfBytes = java.util.Base64.getDecoder().decode(createDTO.getSyllabusPath());

                // Generate a clean filename for disk persistence
                String uniqueFileName = System.currentTimeMillis() + "_syllabus.pdf";
                Path destinationPath = this.rootLocation.resolve(Paths.get(uniqueFileName)).normalize().toAbsolutePath();

                // Save the byte array onto your storage folder location path
                Files.write(destinationPath, pdfBytes);

                // Set the local path key directly to your Course entity column model
                course.setSyllabusPath(uniqueFileName);

            } catch (IllegalArgumentException e) {
                throw new BusinessException("The uploaded syllabus file string data is not a valid Base64 configuration format.");
            } catch (IOException e) {
                throw new BusinessException("Failed to safely write decoded syllabus PDF files to server storage.");
            }
        }

        return registrarMapper.toRegistrarCourseResponseDTO(courseRepository.save(course));
    }


    @Override
    @AuditEvent(eventName = "ALL_COURSES_FETCHED", eventType = "READ", eventMessage = "All configured courses were fetched")
    public List<RegistrarCourseResponseDTO> getAllConfiguredCourses() {
        verifyRegistrarContext();
        return courseRepository.findAll().stream()
                .map(registrarMapper::toRegistrarCourseResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @AuditEvent(eventName = "FILTER_APPLIED", eventType = "READ", eventMessage = "Registrar applied filter on students or instructors")
    public Object filterByRole(String role, String name, String status,
                               String fieldOfInterest, String enrolledCourse,
                               String skill, Integer experience,
                               String sortBy, String sortDir) {
        verifyRegistrarContext();
        boolean descending = "desc".equalsIgnoreCase(sortDir);
        if ("student".equalsIgnoreCase(role)) {
            return filterStudents(name, status, fieldOfInterest, enrolledCourse, sortBy, descending);
        } else if ("instructor".equalsIgnoreCase(role)) {
            return filterInstructors(name, status, skill, experience, sortBy, descending);
        } else {
            throw new BusinessException("Invalid role '" + role + "'. Allowed values: student, instructor");
        }
    }

    private List<StudentFilterOutputDTO> filterStudents(String name, String status, String fieldOfInterest, String enrolledCourse, String sortBy, boolean descending) {
        List<Student> students = studentRepository.findAll();
        List<StudentFilterOutputDTO> result = students.stream()
                .filter(s -> name == null || s.getUser().getName().toLowerCase().contains(name.toLowerCase()))
                .filter(s -> fieldOfInterest == null || (s.getFieldOfInterest() != null && s.getFieldOfInterest().toLowerCase().contains(fieldOfInterest.toLowerCase())))
                .map(s -> {
                    List<String> courses = enrollmentRepository.findByStudent_StudentId(s.getStudentId())
                            .stream().map(e -> e.getCourse().getTitle()).collect(Collectors.toList());
                    return StudentFilterOutputDTO.builder()
                            .studentId(s.getStudentId())
                            .name(s.getUser().getName())
                            .email(s.getUser().getEmail())
                            .fieldOfInterest(s.getFieldOfInterest())
                            .enrolledCourses(courses)
                            .build();
                })
                .filter(s -> enrolledCourse == null || s.getEnrolledCourses().stream().anyMatch(c -> c.toLowerCase().contains(enrolledCourse.toLowerCase())))
                .collect(Collectors.toList());

        Comparator<StudentFilterOutputDTO> comparator = switch (sortBy != null ? sortBy.toLowerCase() : "name") {
            case "fieldofinterest" -> Comparator.comparing(StudentFilterOutputDTO::getFieldOfInterest, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(StudentFilterOutputDTO::getName, Comparator.nullsLast(String::compareToIgnoreCase));
        };
        if (descending) comparator = comparator.reversed();
        result.sort(comparator);
        return result;
    }

    private List<InstructorFilterOutputDTO> filterInstructors(String name, String status, String skill, Integer experience, String sortBy, boolean descending) {
        List<Instructor> instructors = instructorRepository.findAll();
        List<InstructorFilterOutputDTO> result = instructors.stream()
                .filter(i -> name == null || i.getUser().getName().toLowerCase().contains(name.toLowerCase()))
                .filter(i -> skill == null || (i.getSkills() != null && i.getSkills().stream()
                        .anyMatch(s -> s.name().toLowerCase().contains(skill.toLowerCase()))))
                .filter(i -> experience == null || (i.getExperience() != null && i.getExperience() >= experience))
                .map(i -> {
                    List<String> courses = courseRepository.findByInstructor_InstructorId(i.getInstructorId())
                            .stream().map(Course::getTitle).collect(Collectors.toList());

                    String skillString = (i.getSkills() != null) ? i.getSkills().stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")) : null;

                    return InstructorFilterOutputDTO.builder()
                            .instructorId(i.getInstructorId())
                            .name(i.getUser().getName())
                            .email(i.getUser().getEmail())
                            .skill(skillString)
                            .experience(i.getExperience())
                            .assignedCourses(courses)
                            .build();
                })
                .collect(Collectors.toList());

        Comparator<InstructorFilterOutputDTO> comparator = switch (sortBy != null ? sortBy.toLowerCase() : "name") {
            case "experience" -> Comparator.comparing(InstructorFilterOutputDTO::getExperience, Comparator.nullsLast(Integer::compareTo));
            case "status" -> Comparator.comparing(InstructorFilterOutputDTO::getStatus, Comparator.nullsLast(String::compareToIgnoreCase));
            default -> Comparator.comparing(InstructorFilterOutputDTO::getName, Comparator.nullsLast(String::compareToIgnoreCase));
        };
        if (descending) comparator = comparator.reversed();
        result.sort(comparator);
        return result;
    }
}