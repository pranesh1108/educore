package com.cts.dto;

import com.cts.enumerate.InstructorSkill;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorOutputDTO {

    private Long instructorId;
    private List<InstructorSkill> skills;
    private Integer experience;
    private LocalDate dateOfBirth;

    private Long userId;
    private String name;
    private String email;
    private Long phone;
    private String role;
}