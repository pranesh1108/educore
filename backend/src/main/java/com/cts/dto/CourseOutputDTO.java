package com.cts.dto;

import com.cts.enumerate.Prerequisite;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseOutputDTO {

    private Long courseId;
    private String title;
    private String description;
    private String syllabusPath;
    private Prerequisite prerequisite;
    private Long instructorId;
    private String instructorName;
}
