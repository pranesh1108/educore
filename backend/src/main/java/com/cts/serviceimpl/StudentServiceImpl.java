package com.cts.serviceimpl;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cts.annotation.AuditEvent;
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.exception.*;
import com.cts.mapper.ExamMapper;
import com.cts.repository.ExamRepository;
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
            if (age < 18 || age > 100) {
                throw new BusinessException("Student age must be between 18 and 100 years old.");
            }
            if (inputDTO.getFieldOfInterest() != null && !inputDTO.getFieldOfInterest().matches("^[a-zA-Z_, ]*$")) {
                throw new BusinessException("Field of interest contains invalid characters");
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
    @AuditEvent(eventName = "COURSE_CATALOGUE_FILTERED", eventType = "READ", eventMessage = "Course catalogue was filtered with pagination")
    public Page<RegistrarCourseResponseDTO> filterCourses(String title, String topic, Pageable pageable) {
        Page<Course> coursesPage = courseRepository.filterCourses(title, topic, pageable);
        LocalDate today = LocalDate.now();

        List<RegistrarCourseResponseDTO> filteredList = coursesPage.getContent().stream()
                .filter(c -> c.getEnrollmentDeadlineDate() == null || !today.isAfter(c.getEnrollmentDeadlineDate()))
                .map(studentMapper::toRegistrarCourseResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(filteredList, pageable, coursesPage.getTotalElements());
    }

    @Override
    @Transactional
    public EnrollmentOutputDTO enrollInCourse(Long courseId) {
        Student student = getLoggedInStudent();

        boolean alreadyEnrolled = enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseId(
                student.getStudentId(), courseId
        );
        if (alreadyEnrolled) {
            throw new EnrollmentException("Database constraint violation: This entry or relationship already exists.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        if (course.getEnrollmentDeadlineDate() != null && LocalDate.now().isAfter(course.getEnrollmentDeadlineDate())) {
            throw new EnrollmentException("The registration deadline for this track has passed.");
        }

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);

        enrollment.setEnrolledAt(LocalDate.now());

        enrollment.setEnrollmentNumber("ENR-" + System.currentTimeMillis());

        CourseEnrollment saved = enrollmentRepository.save(enrollment);
        return studentMapper.toEnrollmentOutputDTO(saved);
    }

    @Override
    @AuditEvent(eventName = "ENROLLED_COURSES_FETCHED", eventType = "READ", eventMessage = "Student fetched their enrolled courses")
    public List<EnrollmentOutputDTO> getMyEnrolledCourses() {
        Student student = getLoggedInStudent();
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent_StudentId(student.getStudentId());

        if (enrollments.isEmpty()) {
            return new java.util.ArrayList<>();
        }

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

    @Override
    @AuditEvent(eventName = "COURSE_SYLLABUS_STREAMED", eventType = "READ", eventMessage = "Syllabus resource generated from disk by course ID")
    public Resource getSyllabusResource(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        if (course.getSyllabusPath() == null || course.getSyllabusPath().isBlank()) {
            throw new ResourceNotFoundException("No syllabus file path registered for course id: " + courseId);
        }

        try {
            Path rootLocation = Paths.get("uploads/syllabi");
            Path file = rootLocation.resolve(course.getSyllabusPath()).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Syllabus PDF file could not be read or found on server storage disk.");
            }
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception e) {
            throw new BusinessException("Internal server failure while processing syllabus asset streams.");
        }
    }

    private void verifyEnrollment(Long studentId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));
        // 1. Verify student is actually enrolled
        if (!enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseId(studentId, courseId)) {
            throw new NotEnrolledException("You are not enrolled in course id: " + courseId + ". Please enroll first.");
        }
        // 2. Check if course hasn't started yet
        LocalDate today = LocalDate.now();
        if (course.getStartDate() != null && today.isBefore(course.getStartDate())) {
            throw new AccessDeniedException("Access Deferred: This course content can only be accessed after its start date: " + course.getStartDate());
        }
        // 3. Check if exam results have been published (course completed)
        if (examResultRepository.existsByStudent_StudentIdAndCourse_CourseId(studentId, courseId)) {
            throw new BusinessException("Course access closed: Exam results have already been published for this course.");
        }

    }

}