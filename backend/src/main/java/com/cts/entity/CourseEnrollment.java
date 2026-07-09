package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "course_enrollment",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"student_id", "course_id"},
               name = "uk_student_course"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrollment_number", length = 30)
    private String enrollmentNumber;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDate enrolledAt;

    // ACTIVE / DROPPED
    @Column(name = "status", nullable = false)
    private String status;
}
