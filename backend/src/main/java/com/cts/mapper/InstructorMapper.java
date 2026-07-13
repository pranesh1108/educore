package com.cts.mapper;

import org.springframework.stereotype.Component;
import com.cts.dto.InstructorOutputDTO;
import com.cts.entity.Instructor;

@Component
public class InstructorMapper {

    public InstructorOutputDTO toInstructorOutputDTO(Instructor instructor) {
        return InstructorOutputDTO.builder()
                .instructorId(instructor.getInstructorId())
                .skills(instructor.getSkills())
                .experience(instructor.getExperience())
                .dateOfBirth(instructor.getDateOfBirth())
                .userId(instructor.getUser().getUserId())
                .name(instructor.getUser().getName())
                .email(instructor.getUser().getEmail())
                .phone(instructor.getUser().getPhone())
                .role(instructor.getUser().getRole().name())
                .build();
    }
}