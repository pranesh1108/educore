package com.cts.dto;

import com.cts.enumerate.Prerequisite;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarCourseResponseDTO {

    private Long courseId;
    private String title;
    private String description;
    private Prerequisite prerequisite;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate enrollmentDeadlineDate;
    private Long instructorId;
    private String instructorName;
    private String instructorEmail;
    private String instructorSkill;
    private String syllabusPath;
}