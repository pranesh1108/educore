package com.cts.entity;

import com.cts.enumerate.ExamResult;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_result",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"exam_id", "student_id"},
               name = "uk_exam_student_result"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "score", nullable = false)
    private Double score;


    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private ExamResult result;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;
}
