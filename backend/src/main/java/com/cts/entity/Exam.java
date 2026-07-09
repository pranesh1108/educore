package com.cts.entity;

import com.cts.enumerate.AcademicTerm;
import com.cts.enumerate.ExamStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exams")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Long examId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "term", nullable = false)
    private AcademicTerm term;

    @Column(name = "exam_date")
    private LocalDateTime examDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "total_marks")
    private Integer totalMarks;

    @Column(name = "passing_marks")
    private Integer passingMarks;

    @Convert(converter = com.cts.enumerate.ExamStatusConverter.class)
    @Column(name = "status", nullable = false)
    private ExamStatus status;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
