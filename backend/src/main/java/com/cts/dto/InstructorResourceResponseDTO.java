package com.cts.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorResourceResponseDTO {
    private List<CourseMaterialFileOutputDTO> materials;
    private List<AssignmentFileOutputDTO> assignmentFiles;
}