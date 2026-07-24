package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_room_allocation",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_student_exam",
                   columnNames = {"student_id", "exam_id"})
       })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamRoomAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Long allocationId;

    @ManyToOne
    @JoinColumn(name = "exam_room_id", nullable = false)
    private ExamRoom examRoom;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "allocated_at")
    private LocalDateTime allocatedAt;
}