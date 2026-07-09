package com.cts.entity;

import com.cts.enumerate.InstructorSkill;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "instructor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instructor_id")
    private Long instructorId;

    // Refactored to an ElementCollection mapping table
    @ElementCollection(targetClass = InstructorSkill.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "instructor_skills", joinColumns = @JoinColumn(name = "instructor_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "skill")
    private List<InstructorSkill> skills;

    @Column(name = "experience")
    private Integer experience;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "status")
    private String status;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;
}