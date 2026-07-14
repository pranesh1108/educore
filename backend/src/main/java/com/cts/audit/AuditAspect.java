package com.cts.audit;

<<<<<<< HEAD
=======
import org.aspectj.lang.JoinPoint;
>>>>>>> 37751a7 (update the main code)
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.cts.annotation.AuditEvent;
import com.cts.entity.AuditLog;
<<<<<<< HEAD
import com.cts.repository.AuditLogRepository;
import lombok.AllArgsConstructor;
=======
import com.cts.entity.Notification;
import com.cts.repository.AuditLogRepository;
import com.cts.repository.NotificationRepository;
import com.cts.repository.StudentRepository;
import com.cts.repository.CourseEnrollmentRepository;
import com.cts.repository.ExamRepository;
import com.cts.entity.CourseEnrollment;
import com.cts.entity.Exam;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
>>>>>>> 37751a7 (update the main code)

@Aspect
@Component
@AllArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
<<<<<<< HEAD

    @AfterReturning("@annotation(auditEvent)")
    public void logAudit(AuditEvent auditEvent) {
=======
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final ExamRepository examRepository;

    @AfterReturning(value = "@annotation(auditEvent)", returning = "result")
    public void logAudit(JoinPoint joinPoint, AuditEvent auditEvent, Object result) {
>>>>>>> 37751a7 (update the main code)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String doneBy = (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName()))
                ? auth.getName()
                : "anonymous";

<<<<<<< HEAD
=======
        // 1. Maintain existing Audit Trail functionality completely untouched
>>>>>>> 37751a7 (update the main code)
        AuditLog log = new AuditLog();
        log.setEventName(auditEvent.eventName());
        log.setEventType(auditEvent.eventType());
        log.setEventMessage(auditEvent.eventMessage());
        log.setDoneBy(doneBy);
        auditLogRepository.save(log);
<<<<<<< HEAD
=======

        // 2. ── DYNAMIC DISPATCH LOGIC FOR USER NOTIFICATIONS ──
        try {
            String eventName = auditEvent.eventName();
            Object[] args = joinPoint.getArgs();

            switch (eventName) {
                case "SUBMISSION_GRADED":
                    // Target specific student whose assignment was evaluated
                    com.cts.dto.SubmissionOutputDTO submissionResult = (com.cts.dto.SubmissionOutputDTO) result;
                    studentRepository.findById(submissionResult.getStudentId()).ifPresent(student ->
                            createNotification(student.getUser().getEmail(), "Assignment Graded",
                                    "Your submission for '" + submissionResult.getAssignmentTitle() +
                                            "' in course '" + submissionResult.getCourseTitle() + "' has been graded. Score: " + submissionResult.getGrade())
                    );
                    break;

                case "ASSIGNMENT_PUBLISHED":
                    // Target all students enrolled in the parent course
                    com.cts.dto.AssignmentOutputDTO assignmentResult = (com.cts.dto.AssignmentOutputDTO) result;
                    List<CourseEnrollment> studentsInCourse = enrollmentRepository.findByCourse_CourseId(assignmentResult.getCourseId());
                    for (CourseEnrollment enrollment : studentsInCourse) {
                        createNotification(enrollment.getStudent().getUser().getEmail(), "New Assignment Released",
                                "A new evaluation assignment task '" + assignmentResult.getTitle() +
                                        "' has been published in your course. Total Marks: " + assignmentResult.getTotalMarks());
                    }
                    break;

                case "EXAM_RESULT_PUBLISHED":
                    // Target student whose exam terms are concluded
                    com.cts.dto.ExamResultOutputDTO resultDTO = (com.cts.dto.ExamResultOutputDTO) result;
                    studentRepository.findById(resultDTO.getStudentId()).ifPresent(student ->
                            createNotification(student.getUser().getEmail(), "Final Exam Results Out",
                                    "Your final exam score for course '" + resultDTO.getCourseTitle() +
                                            "' has been published. Outcome: " + resultDTO.getResult())
                    );
                    break;

                case "ASSIGNMENT_SUBMITTED":
                    // Target instructor assigned to that course to review evaluation task
                    com.cts.dto.SubmissionOutputDTO subDTO = (com.cts.dto.SubmissionOutputDTO) result;
                    // Find exam room or enrollment path to get course instructor email context
                    createNotification(doneBy, "Submission Successful",
                            "Your assignment file '" + subDTO.getFileName() + "' was successfully processed.");
                    break;

                case "EXAM_ROOM_ASSIGNED":
                    // Target all students allocated to this exam batch slot
                    com.cts.dto.ExamRoomOutputDTO roomDTO = (com.cts.dto.ExamRoomOutputDTO) result;
                    examRepository.findById(roomDTO.getExamId()).ifPresent(exam -> {
                        List<CourseEnrollment> enrolledList = enrollmentRepository.findByCourse_CourseId(exam.getCourse().getCourseId());
                        for (CourseEnrollment enr : enrolledList) {
                            createNotification(enr.getStudent().getUser().getEmail(), "Exam Venue Allocated",
                                    "Your exam venue room for '" + roomDTO.getExamTitle() +
                                            "' has been dynamically assigned to room number: " + roomDTO.getRoomNumber());
                        }
                    });
                    break;
            }
        } catch (Exception e) {
            // Safety block to ensure notification dispatch failure never impacts core transaction processing
            System.err.println("Background notification processing exception deferred: " + e.getMessage());
        }
    }

    private void createNotification(String email, String title, String message) {
        Notification notification = Notification.builder()
                .userEmail(email)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
>>>>>>> 37751a7 (update the main code)
    }
}