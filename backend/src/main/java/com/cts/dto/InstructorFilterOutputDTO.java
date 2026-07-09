package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorFilterOutputDTO {

    private Long instructorId;
    private String name;
    private String email;
    private String skill;
    private Integer experience;
    private String status;

    // Courses assigned to this instructor
    private List<String> assignedCourses;
}
