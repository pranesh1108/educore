package com.cts.serviceimpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cts.dto.InstructorInputDTO;
import com.cts.dto.InstructorOutputDTO;
import com.cts.entity.Instructor;
import com.cts.entity.User;
import com.cts.enumerate.InstructorSkill;
import com.cts.enumerate.Role;
import com.cts.exception.BusinessException;
import com.cts.repository.InstructorRepository;
import com.cts.mapper.InstructorMapper;

public class InstructorServiceImplTest {

    @Mock private InstructorRepository instructorRepository;
    @Mock private InstructorMapper instructorMapper;

    @InjectMocks
    private InstructorServiceImpl instructorService;

    private Instructor instructor;
    private InstructorInputDTO inputDTO;
    private InstructorOutputDTO outputDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = User.builder().userId(1L).name("Aditya").role(Role.INSTRUCTOR).build();
        instructor = Instructor.builder().instructorId(1L).user(user).build();

        inputDTO = InstructorInputDTO.builder()
                .instructorId(1L).skill(InstructorSkill.JAVA).experience(5)
                .dateOfBirth(LocalDate.of(1990, 1, 1)).build();

        outputDTO = InstructorOutputDTO.builder()
                .instructorId(1L).skill(InstructorSkill.JAVA).experience(5).name("Aditya").build();
    }

    @Test
    void updateInstructorProfile_success() {
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(instructorRepository.save(instructor)).thenReturn(instructor);
        when(instructorMapper.toInstructorOutputDTO(instructor)).thenReturn(outputDTO);

        InstructorOutputDTO result = instructorService.updateInstructorProfile(inputDTO);

        assertNotNull(result);
        assertEquals(InstructorSkill.JAVA, instructor.getSkill());
        assertEquals(5, instructor.getExperience());
    }

    @Test
    void updateInstructorProfile_ageLessThan25_throwsBusinessException() {
        inputDTO.setDateOfBirth(LocalDate.now().minusYears(20));
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));

        assertThrows(BusinessException.class, () -> instructorService.updateInstructorProfile(inputDTO));
    }
}