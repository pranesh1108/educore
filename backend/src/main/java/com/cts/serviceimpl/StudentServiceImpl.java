package com.cts.serviceimpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import com.cts.annotation.AuditEvent;
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.exception.*;
import com.cts.mapper.ExamMapper;
import com.cts.repository.ExamRepository;
import com.cts.dto.ExamOutputDTO;
import com.cts.mapper.AssignmentMapper;
import com.cts.mapper.CourseMapper;
import com.cts.mapper.StudentMapper;
import com.cts.repository.*;
import com.cts.service.FileStorageService;
import com.cts.service.StudentService;
import com.cts.util.SecurityUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final ExamRepository examRepository;
    private final ExamMapper examMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final AssignmentMapper assignmentMapper;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentFileRepository assignmentFileRepository;
    private final CourseMaterialFileRepository courseMaterialFileRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final FileStorageService fileStorageService;
    private final ExamResultRepository examResultRepository;

    // private final Path syllabusRootLocation = Paths.get("uploads/syllabi");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Student getLoggedInStudent() {
        String loggedInEmail = SecurityUtils.getLoggedInEmail();
        return studentRepository
                .findByUser_Email(loggedInEmail)
                .orElseThrow(() -> new StudentNotFoundException(
                        "Student profile not found for logged-in user credentials."));
    }

    @Override
    @AuditEvent(eventName = "STUDENT_PROFILE_UPDATED", eventType = "UPDATE", eventMessage = "Student profile was updated")
    public StudentOutputDTO updateStudentProfile(StudentInputDTO inputDTO) {
        Student student = getLoggedInStudent();

        if (inputDTO.getDateOfBirth() != null) {
            int age = Period.between(inputDTO.getDateOfBirth(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new BusinessException("Student must be at least 18 years old. Age: " + age);
            }
        }
        student.setDateOfBirth(inputDTO.getDateOfBirth());
        student.setFieldOfInterest(inputDTO.getFieldOfInterest());
        return studentMapper.tostudentOutputDTO(studentRepository.save(student));
    }

    @Override
    @AuditEvent(eventName = "STUDENT_FETCHED", eventType = "READ", eventMessage = "Student details were fetched by context session")
    public StudentOutputDTO getStudentProfile() {
        return studentMapper.tostudentOutputDTO(getLoggedInStudent());
    }

    @Override
    @AuditEvent(eventName = "ALL_COURSES_VIEWED_BY_STUDENT", eventType = "READ", eventMessage = "Student viewed all available courses")
    public List<RegistrarCourseResponseDTO> getAllCourses() {
        LocalDate today = LocalDate.now();

        // Filters and fetches courses where today is before OR EQUAL TO the enrollment deadline date
        List<Course> courses = courseRepository.findAll().stream()
                .filter(c -> c.getEnrollmentDeadlineDate() == null || !today.isAfter(c.getEnrollmentDeadlineDate()))
                .collect(Collectors.toList());

        return courses.stream()
                .map(studentMapper::toRegistrarCourseResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @AuditEvent(eventName = "COURSE_CATALOGUE_FILTERED", eventType = "READ", eventMessage = "Course catalogue was filtered with pagination")
    public Page<RegistrarCourseResponseDTO> filterCourses(String title, String topic, Pageable pageable) {
        Page<Course> coursesPage = courseRepository.filterCourses(title, topic, pageable);
        LocalDate today = LocalDate.now();

        List<RegistrarCourseResponseDTO> filteredList = coursesPage.getContent().stream()
                .filter(c -> c.getEnrollmentDeadlineDate() == null || !today.isAfter(c.getEnrollmentDeadlineDate()))
                .map(studentMapper::toRegistrarCourseResponseDTO)
                .collect(Collectors.toList());

        if (filteredList.isEmpty()) {
            throw new NoDetailsAvailableException("No courses match the given filters on this page.");
        }

        return new PageImpl<>(filteredList, pageable, coursesPage.getTotalElements());
    }

    @Override
    @AuditEvent(eventName = "STUDENT_ENROLLED", eventType = "CREATE", eventMessage = "Student self-enrolled in a course")
    public EnrollmentOutputDTO enrollInCourse(Long courseId) {
        Student student = getLoggedInStudent();
        Long studentId = student.getStudentId();

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        LocalDate today = LocalDate.now();
        if (course.getEnrollmentDeadlineDate() != null && today.isAfter(course.getEnrollmentDeadlineDate())) {
            throw new BusinessException("Enrollment Closed: The deadline date for this course has passed.");
        }

        List<ExamResultEntity> studentResults = examResultRepository.findByStudent_StudentId(studentId);
        for (ExamResultEntity result : studentResults) {
            if (result.getCourse().getCourseId().equals(courseId)) {
                throw new BusinessException("Enrollment Closed: An exam score has already been published for this course.");
            }
        }

        if (enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseId(studentId, courseId)) {
            throw new EnrollmentException("You are already enrolled in course: " + course.getTitle());
        }

        String enrollmentNumber = generateEnrollmentNumber(course.getTitle(), studentId);

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .student(student).course(course)
                .enrollmentNumber(enrollmentNumber)
                .enrolledAt(LocalDate.now()).status("ACTIVE").build();
        return studentMapper.toEnrollmentOutputDTO(enrollmentRepository.save(enrollment));
    }

    @Override
    @AuditEvent(eventName = "ENROLLED_COURSES_FETCHED", eventType = "READ", eventMessage = "Student fetched their enrolled courses")
    public List<EnrollmentOutputDTO> getMyEnrolledCourses() {
        Student student = getLoggedInStudent();
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent_StudentId(student.getStudentId());
        if (enrollments.isEmpty()) throw new EnrollmentException("You are not enrolled in any courses yet.");
        //return Collections.emptyList();
        return enrollments.stream().map(studentMapper::toEnrollmentOutputDTO).collect(Collectors.toList());
    }

    @Override
    @AuditEvent(eventName = "COURSE_CONTENT_VIEWED", eventType = "READ", eventMessage = "Student viewed course assignments and materials simultaneously")
    public CourseContentResponseDTO getCourseContent(Long courseId) {
        Student student = getLoggedInStudent();
        verifyEnrollment(student.getStudentId(), courseId);

        List<CourseMaterialFileOutputDTO> materials = courseMaterialFileRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(courseMapper::toCourseMaterialFileOutputDTO)
                .collect(Collectors.toList());

        List<Assignment> assignments = assignmentRepository.findByCourse_CourseId(courseId);
        List<AssignmentOutputDTO> assignmentDTOs = assignments.stream().map(a -> {
            List<AssignmentFileOutputDTO> files = assignmentFileRepository
                    .findByAssignment_AssignmentId(a.getAssignmentId())
                    .stream()
                    .map(assignmentMapper::toAssignmentFileOutputDTO)
                    .collect(Collectors.toList());
            return studentMapper.toAssignmentOutputDTO(a, files);
        }).collect(Collectors.toList());

        return CourseContentResponseDTO.builder()
                .materials(materials)
                .assignments(assignmentDTOs)
                .build();
    }

    @Override
    @AuditEvent(eventName = "COURSE_MATERIAL_DOWNLOADED", eventType = "READ", eventMessage = "Student downloaded a course material file")
    public byte[] downloadCourseMaterialFile(Long fileId) {
        Student student = getLoggedInStudent();
        CourseMaterialFile materialFile = courseMaterialFileRepository.findById(fileId)
                .orElseThrow(() -> new InvalidFileException("Material file not found with id: " + fileId));
        verifyEnrollment(student.getStudentId(), materialFile.getCourse().getCourseId());
        return fileStorageService.loadFile(materialFile.getFilePath());
    }

    @Override
    public String getCourseMaterialFileName(Long fileId) {
        Student student = getLoggedInStudent();
        CourseMaterialFile materialFile = courseMaterialFileRepository.findById(fileId)
                .orElseThrow(() -> new InvalidFileException("Material file not found with id: " + fileId));
        verifyEnrollment(student.getStudentId(), materialFile.getCourse().getCourseId());
        return materialFile.getFileName();
    }

    @Override
    @AuditEvent(eventName = "ASSIGNMENT_FILE_DOWNLOADED", eventType = "READ", eventMessage = "Student downloaded an assignment file")
    public byte[] downloadAssignmentFile(Long fileId) {
        Student student = getLoggedInStudent();
        AssignmentFile assignmentFile = assignmentFileRepository.findById(fileId)
                .orElseThrow(() -> new InvalidFileException("Assignment file not found with id: " + fileId));
        verifyEnrollment(student.getStudentId(), assignmentFile.getAssignment().getCourse().getCourseId());
        return fileStorageService.loadFile(assignmentFile.getFilePath());
    }

    @Override
    public String getAssignmentFileName(Long fileId) {
        Student student = getLoggedInStudent();
        AssignmentFile assignmentFile = assignmentFileRepository.findById(fileId)
                .orElseThrow(() -> new InvalidFileException("Assignment file not found with id: " + fileId));
        verifyEnrollment(student.getStudentId(), assignmentFile.getAssignment().getCourse().getCourseId());
        return assignmentFile.getFileName();
    }

    @Override
    @AuditEvent(eventName = "ASSIGNMENT_SUBMITTED", eventType = "CREATE", eventMessage = "Student submitted an assignment")
    public SubmissionOutputDTO submitAssignment(Long assignmentId, MultipartFile file) {
        Student student = getLoggedInStudent();
        Long studentId = student.getStudentId();

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with id: " + assignmentId));
        verifyEnrollment(studentId, assignment.getCourse().getCourseId());

        CourseEnrollment enrollment = enrollmentRepository
                .findByStudent_StudentIdAndCourse_CourseId(studentId, assignment.getCourse().getCourseId())
                .orElseThrow(() -> new NotEnrolledException("Enrollment not found"));

        int existingCount = submissionRepository
                .findByStudent_StudentIdAndCourse_CourseId(studentId, assignment.getCourse().getCourseId()).size();
        int nextNumber = existingCount + 1;

        String autoName = fileStorageService.generateSubmissionFileName(
                assignment.getCourse().getTitle(), nextNumber);

        LocalDateTime now = LocalDateTime.now();
        String submissionStatus = "SUBMITTED";
        if (assignment.getDueDate() != null && now.isAfter(assignment.getDueDate())) {
            submissionStatus = "LATE_SUBMISSION";
        }

        String savedPath = fileStorageService.storeFile(file, "submissions", autoName);

        Submission submission = Submission.builder()
                .student(student).assignment(assignment)
                .course(assignment.getCourse())
                .filePath(savedPath).fileName(autoName).enrollmentNumber(enrollment.getEnrollmentNumber())
                .submittedAt(now).status(submissionStatus).build();

        return studentMapper.toSubmissionOutputDTO(submissionRepository.save(submission));
    }

    @Override
    @AuditEvent(eventName = "MY_SUBMISSIONS_FETCHED", eventType = "READ", eventMessage = "Student fetched their own submissions")
    public List<SubmissionOutputDTO> getMySubmissions() {
        Student student = getLoggedInStudent();
        List<Submission> submissions = submissionRepository.findByStudent_StudentId(student.getStudentId());
        if (submissions.isEmpty())
            throw new SubmissionNotFoundException("No submissions found for student id: " + student.getStudentId());
        return submissions.stream().map(studentMapper::toSubmissionOutputDTO).collect(Collectors.toList());
    }

    @Override
    @AuditEvent(eventName = "MY_EXAMS_FETCHED", eventType = "READ", eventMessage = "Exams assigned to student are fetched")
    public List<ExamOutputDTO> getMyExams() {
        Student student = getLoggedInStudent();
        List<ExamOutputDTO> exams = examRepository.findExamsForStudent(student.getStudentId())
                .stream().map(examMapper::toExamOutputDTO).collect(Collectors.toList());
        if (exams.isEmpty())
            throw new NoDetailsAvailableException("No exams found for student : " + student.getUser().getName());
        return exams;
    }

    private void verifyEnrollment(Long studentId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        LocalDate today = LocalDate.now();
        if (course.getEnrollmentDeadlineDate() != null && today.isAfter(course.getEnrollmentDeadlineDate())) {
            throw new AccessDeniedException("Access Closed: The enrollment deadline for this course has passed.");
        }
        if (course.getStartDate() != null && today.isBefore(course.getStartDate())) {
            throw new AccessDeniedException("Access Deferred: This course content can only be accessed after its start date: " + course.getStartDate());
        }

        if (!enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseId(studentId, courseId)) {
            throw new NotEnrolledException("You are not enrolled in course id: " + courseId + ". Please enroll first.");
        }
        if (examResultRepository.existsByStudent_StudentIdAndCourse_CourseId(studentId, courseId)) {
            throw new BusinessException("Course access closed: Exam results have already been published for this course.");
        }
    }

    private String generateEnrollmentNumber(String courseTitle, Long studentId) {
        String code = courseTitle.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (code.length() > 6) code = code.substring(0, 6);
        return code + String.format("%04d", studentId);
    }



}