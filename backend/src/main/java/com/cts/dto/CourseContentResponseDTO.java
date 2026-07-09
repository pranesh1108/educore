package com.cts.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseContentResponseDTO {
    private List<CourseMaterialFileOutputDTO> materials;
    private List<AssignmentOutputDTO> assignments;
}