package com.cts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentOutputDTO {

    private Long assignmentId;
    private String title;
    private String instructions;
    private Double totalMarks;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime publishedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dueDate;

    private Long courseId;
    private String courseTitle;

    // Files attached to this assignment (PDF attachments uploaded by instructor)
    private List<AssignmentFileOutputDTO> files;
}
