package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "physical_rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PhysicalRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", nullable = false, unique = true)
    private String roomName;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "assigned_exam_id")
    private Long assignedExamId;

    @Column(name = "assigned_from")
    private LocalDateTime assignedFrom;

    @Column(name = "assigned_until")
    private LocalDateTime assignedUntil;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
