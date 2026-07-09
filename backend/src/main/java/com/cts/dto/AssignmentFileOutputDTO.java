package com.cts.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentFileOutputDTO {

    private Long fileId;
    private Long assignmentId;
    private String assignmentTitle;
    private String fileName;
    private LocalDateTime uploadedAt;
}