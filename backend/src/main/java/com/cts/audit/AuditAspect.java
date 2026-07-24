package com.cts.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.cts.annotation.AuditEvent;
import com.cts.entity.AuditLog;
import com.cts.repository.AuditLogRepository;
import lombok.AllArgsConstructor;
import com.cts.entity.Notification;
import com.cts.repository.NotificationRepository;
import com.cts.repository.StudentRepository;
import com.cts.repository.CourseEnrollmentRepository;
import com.cts.repository.ExamRepository;
import com.cts.repository.InstructorRepository;
import com.cts.entity.CourseEnrollment;
import com.cts.entity.Student;
import java.time.LocalDateTime;
import java.util.List;

@Aspect
@Component
@AllArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final ExamRepository examRepository;
    private final InstructorRepository instructorRepository;

    @AfterReturning(value = "@annotation(auditEvent)", returning = "result")
    public void logAudit(JoinPoint joinPoint, AuditEvent auditEvent, Object result) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String doneBy = (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName()))
                ? auth.getName()
                : "anonymous";

        AuditLog log = new AuditLog();
        log.setEventName(auditEvent.eventName());
        log.setEventType(auditEvent.eventType());
        log.setEventMessage(auditEvent.eventMessage());
        log.setDoneBy(doneBy);
        auditLogRepository.save(log);

        try {
            String eventName = auditEvent.eventName();

            switch (eventName) {

                case "COURSE_PROVISIONED": {
                    com.cts.dto.RegistrarCourseResponseDTO courseDTO = (com.cts.dto.RegistrarCourseResponseDTO) result;

                    if (courseDTO.getInstructorEmail() != null) {
                        createNotification(
                                courseDTO.getInstructorEmail(),
                                "New Course Assigned",
                                "You have been assigned as the instructor for course: '" + courseDTO.getTitle() + "'."
                        );
                    }

                    // Notify All Registered Students about the new course
                    List<Student> allStudents = studentRepository.findAll();
                    for (Student s : allStudents) {
                        if (s.getUser() != null && s.getUser().getEmail() != null) {
                            createNotification(
                                    s.getUser().getEmail(),
                                    "New Course Catalogue Offering",
                                    "A new course '" + courseDTO.getTitle() + "' has been published. Enrollment deadline: " + courseDTO.getEnrollmentDeadlineDate()
                            );
                        }
                    }
                    break;
                }

                case "COURSE_MATERIAL_PUBLISHED": {
                    com.cts.dto.CourseMaterialFileOutputDTO materialDTO = (com.cts.dto.CourseMaterialFileOutputDTO) result;

                    List<CourseEnrollment> studentsInCourse = enrollmentRepository.findByCourse_CourseId(materialDTO.getCourseId());
                    for (CourseEnrollment enrollment : studentsInCourse) {
                        createNotification(
                                enrollment.getStudent().getUser().getEmail(),
                                "New Handout Uploaded",
                                "A new study material '" + materialDTO.getFileName() + "' has been uploaded to course '" + materialDTO.getCourseTitle() + "'."
                        );
                    }
                    break;
                }

                case "EXAM_CREATED": {
                    com.cts.dto.ExamOutputDTO examDTO = (com.cts.dto.ExamOutputDTO) result;

                    // Notify course instructor
                    instructorRepository.findById(examDTO.getInstructorId()).ifPresent(inst -> {
                        if (inst.getUser() != null) {
                            createNotification(
                                    inst.getUser().getEmail(),
                                    "Exam Scheduled",
                                    "An exam '" + examDTO.getTitle() + "' for course '" + examDTO.getCourseTitle() + "' has been scheduled on " + examDTO.getExamDate()
                            );
                        }
                    });

                    List<CourseEnrollment> enrolledStudents = enrollmentRepository.findByCourse_CourseId(examDTO.getCourseId());
                    for (CourseEnrollment enr : enrolledStudents) {
                        createNotification(
                                enr.getStudent().getUser().getEmail(),
                                "New Exam Scheduled",
                                "An examination '" + examDTO.getTitle() + "' has been scheduled for your course '" + examDTO.getCourseTitle() + "' on " + examDTO.getExamDate()
                        );
                    }
                    break;
                }

                case "SUBMISSION_GRADED": {
                    com.cts.dto.SubmissionOutputDTO submissionResult = (com.cts.dto.SubmissionOutputDTO) result;
                    studentRepository.findById(submissionResult.getStudentId()).ifPresent(student ->
                            createNotification(student.getUser().getEmail(), "Assignment Graded",
                                    "Your submission for '" + submissionResult.getAssignmentTitle() +
                                            "' in course '" + submissionResult.getCourseTitle() + "' has been graded. Score: " + submissionResult.getGrade())
                    );
                    break;
                }

                case "ASSIGNMENT_PUBLISHED": {
                    com.cts.dto.AssignmentOutputDTO assignmentResult = (com.cts.dto.AssignmentOutputDTO) result;
                    List<CourseEnrollment> studentsInCourse = enrollmentRepository.findByCourse_CourseId(assignmentResult.getCourseId());
                    for (CourseEnrollment enrollment : studentsInCourse) {
                        createNotification(enrollment.getStudent().getUser().getEmail(), "New Assignment Released",
                                "A new evaluation assignment task '" + assignmentResult.getTitle() +
                                        "' has been published in your course. Total Marks: " + assignmentResult.getTotalMarks());
                    }
                    break;
                }

                case "EXAM_RESULT_PUBLISHED": {
                    com.cts.dto.ExamResultOutputDTO resultDTO = (com.cts.dto.ExamResultOutputDTO) result;
                    studentRepository.findById(resultDTO.getStudentId()).ifPresent(student ->
                            createNotification(student.getUser().getEmail(), "Final Exam Results Out",
                                    "Your final exam score for course '" + resultDTO.getCourseTitle() +
                                            "' has been published. Outcome: " + resultDTO.getResult())
                    );
                    break;
                }

                case "ASSIGNMENT_SUBMITTED": {
                    com.cts.dto.SubmissionOutputDTO subDTO = (com.cts.dto.SubmissionOutputDTO) result;
                    createNotification(doneBy, "Submission Successful",
                            "Your assignment file '" + subDTO.getFileName() + "' was successfully processed.");
                    break;
                }

                case "EXAM_ROOM_ASSIGNED": {
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
            }
        } catch (Exception e) {
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
    }
}