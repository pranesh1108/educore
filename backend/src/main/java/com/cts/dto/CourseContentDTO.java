package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseContentDTO {

    @NotBlank(message = "Topic is required")
    @Size(max = 100, message = "Topic cannot exceed 100 characters")
    private String topic;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
