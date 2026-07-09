package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_coordinator")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamCoordinator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long coordinatorId;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}