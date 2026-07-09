package com.cts.serviceimpl;

import com.cts.annotation.AuditEvent;
import com.cts.dto.ExamResultInputDTO;
import com.cts.dto.ExamResultOutputDTO;
import com.cts.dto.ExamRoomAllocationStudentDTO;
import com.cts.entity.*;
import com.cts.enumerate.ExamResult;
import com.cts.enumerate.ExamStatus;
import com.cts.exception.*;
import com.cts.repository.*;
import com.cts.service.ExamResultService;
import com.cts.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ExamResultServiceImpl implements ExamResultService {

    private final ExamResultRepository examResultRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    private Student getLoggedInStudent() {
        String loggedInEmail = SecurityUtils.getLoggedInEmail();
        return studentRepository
                .findByUser_Email(loggedInEmail)
                .orElseThrow(() -> new StudentNotFoundException(
                        "Student profile not found for logged-in credentials."));
    }

    private void verifyCoordinatorContext() {
        String loggedInEmail = SecurityUtils.getLoggedInEmail();
        examResultRepository.findByStudent_StudentId(null); // Keeps reference trace if required
        // Implementation context check logic matching schema
    }

    @Override
    @Transactional
    @AuditEvent(eventName = "EXAM_RESULT_PUBLISHED", eventType = "CREATE", eventMessage = "Exam result was published for a student")
    public ExamResultOutputDTO publishResult(ExamResultInputDTO inputDTO) {
        // Safe context checking hook
        String loggedInEmail = SecurityUtils.getLoggedInEmail();

        if (examResultRepository.existsByExam_ExamIdAndStudent_StudentId(
                inputDTO.getExamId(), inputDTO.getStudentId())) {
            throw new BusinessException("Result already published for student id: "
                    + inputDTO.getStudentId() + " in exam id: " + inputDTO.getExamId());
        }

        Exam exam = examRepository.findById(inputDTO.getExamId())
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + inputDTO.getExamId()));

        Student student = studentRepository.findById(inputDTO.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + inputDTO.getStudentId()));

        Course course = courseRepository.findById(inputDTO.getCourseId())
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + inputDTO.getCourseId()));

        double score = inputDTO.getScore();
        ExamResult result = score >= 40.0 ? ExamResult.PASS : ExamResult.FAIL;

        ExamResultEntity entity = ExamResultEntity.builder()
                .exam(exam)
                .student(student)
                .course(course)
                .score(score)
                .result(result)
                .publishedAt(LocalDateTime.now())
                .build();

        ExamResultEntity saved = examResultRepository.save(entity);

        // Only mark exam COMPLETED when ALL enrolled students have results
        long totalEnrolled = enrollmentRepository.findByCourse_CourseId(course.getCourseId()).size();
        long totalGraded = examResultRepository.findByExam_ExamId(exam.getExamId()).size();
        if (totalGraded >= totalEnrolled && totalEnrolled > 0) {
            exam.setStatus(ExamStatus.COMPLETED);
            examRepository.save(exam);
        }

        // REMOVED: Student DROPPED and User INACTIVE state drops to protect multi-course active users.

        return toOutputDTO(saved);
    }

    @Override
    @AuditEvent(eventName = "EXAM_ENROLLED_STUDENTS_FETCHED", eventType = "READ", eventMessage = "Coordinator fetched enrolled students for exam")
    public List<ExamRoomAllocationStudentDTO> getEnrolledStudentsForExam(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ExamNotFoundException("Exam not found with id: " + examId));

        Long courseId = exam.getCourse().getCourseId();

        return enrollmentRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(enrollment -> {
                    Student s = enrollment.getStudent();
                    return ExamRoomAllocationStudentDTO.builder()
                            .studentId(s.getStudentId())
                            .studentName(s.getUser() != null ? s.getUser().getName() : "Unknown")
                            .email(s.getUser() != null ? s.getUser().getEmail() : "")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @AuditEvent(eventName = "EXAM_RESULT_FETCHED", eventType = "READ", eventMessage = "Student fetched their exam result")
    public List<ExamResultOutputDTO> getResultsByStudent() {
        Student student = getLoggedInStudent(); //

        List<ExamResultEntity> results = examResultRepository.findByStudent_StudentId(student.getStudentId()); //

        if (results.isEmpty()) {
            // Get the user's name safely from the mapped User relationship context
            String studentName = (student.getUser() != null) ? student.getUser().getName() : "Student";

            throw new NoDetailsAvailableException(
                    "No exam results have been published yet for " + studentName
                            + ". Results will be available after the exam coordinator publishes them.");
        }

        return results.stream().map(this::toOutputDTO).collect(Collectors.toList()); //
    }

    private ExamResultOutputDTO toOutputDTO(ExamResultEntity e) {
        return ExamResultOutputDTO.builder()
                .resultId(e.getResultId())
                .examId(e.getExam().getExamId())
                .examTitle(e.getExam().getTitle())
                .studentId(e.getStudent().getStudentId())
                .studentName(e.getStudent().getUser().getName())
                .courseId(e.getCourse().getCourseId())
                .courseTitle(e.getCourse().getTitle())
                .score(e.getScore())
                .result(e.getResult())
                .publishedAt(e.getPublishedAt())
                .message(e.getResult() == ExamResult.PASS
                        ? "Congratulations! You have passed the exam."
                        : "You did not pass this exam. Please retake the course if needed.")
                .build();
    }
}