package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "submission")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "assignment_id", referencedColumnName = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrollment_number", length = 30)
    private String enrollmentNumber;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // Filled by instructor after grading
    @Column(name = "grade")
    private Double grade;

    @Column(name = "feedback")
    private String feedback;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    // SUBMITTED / GRADED / LATE_SUBMISSION
    @Column(name = "status", nullable = false)
    private String status;
}
