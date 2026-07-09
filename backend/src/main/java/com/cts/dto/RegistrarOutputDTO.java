package com.cts.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrarOutputDTO {

    private Long registrarId;
    private Integer publishedCourseCount;
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String status;
}