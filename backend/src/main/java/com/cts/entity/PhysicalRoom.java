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

    // Capacity defined at room creation (min 20, max 100)
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    // AVAILABLE or OCCUPIED
    @Column(name = "status", nullable = false)
    private String status;

    // Exam that currently occupies this room (null when AVAILABLE)
    @Column(name = "assigned_exam_id")
    private Long assignedExamId;

    // Time window during which the room is occupied (exam start → exam end)
    @Column(name = "assigned_from")
    private LocalDateTime assignedFrom;

    @Column(name = "assigned_until")
    private LocalDateTime assignedUntil;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
