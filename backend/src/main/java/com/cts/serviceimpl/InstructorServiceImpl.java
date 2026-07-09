package com.cts.serviceimpl;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cts.annotation.AuditEvent;
import com.cts.dto.EnrollmentOutputDTO;
import com.cts.dto.ExamOutputDTO;
import com.cts.dto.InstructorInputDTO;
import com.cts.dto.InstructorOutputDTO;
import com.cts.entity.Instructor;
import com.cts.exception.AccessDeniedException;
import com.cts.exception.BusinessException;
import com.cts.exception.CourseNotFoundException;
import com.cts.exception.InstructorNotFoundException;
import com.cts.mapper.ExamMapper;
import com.cts.mapper.InstructorMapper;
import com.cts.mapper.StudentMapper;
import com.cts.repository.CourseEnrollmentRepository;
import com.cts.repository.CourseRepository;
import com.cts.repository.ExamRepository;
import com.cts.repository.InstructorRepository;
import com.cts.service.InstructorService;
import com.cts.util.SecurityUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InstructorServiceImpl implements InstructorService {

    private final InstructorMapper instructorMapper;
    private final StudentMapper studentMapper;
    private final InstructorRepository instructorRepository;
    private final ExamRepository examRepository;
    private final ExamMapper examMapper;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;

    private Instructor getLoggedInInstructor() {
        String loggedInEmail = SecurityUtils.getLoggedInEmail();
        return instructorRepository
                .findByUser_Email(loggedInEmail)
                .orElseThrow(() -> new InstructorNotFoundException(
                        "Instructor profile not found for logged-in user context."));
    }

    @Override
    @AuditEvent(eventName = "INSTRUCTOR_PROFILE_UPDATED", eventType = "UPDATE", eventMessage = "Instructor profile was updated")
    public InstructorOutputDTO updateInstructorProfile(InstructorInputDTO inputDTO) {
        Instructor instructor = getLoggedInInstructor();

        if (inputDTO.getDateOfBirth() != null) {
            int age = Period.between(inputDTO.getDateOfBirth(), LocalDate.now()).getYears();
            if (age < 25) {
                throw new BusinessException("Instructor must be at least 25 years old.");
            }
        }

        instructor.setSkills(inputDTO.getSkills()); // Saved as collection array list
        instructor.setExperience(inputDTO.getExperience());
        instructor.setDateOfBirth(inputDTO.getDateOfBirth());

        return instructorMapper.toInstructorOutputDTO(instructorRepository.save(instructor));
    }

    @Override
    public InstructorOutputDTO getInstructorProfile() {
        return instructorMapper.toInstructorOutputDTO(getLoggedInInstructor());
    }

    @Override
    public List<ExamOutputDTO> getMyExams() {
        Instructor instructor = getLoggedInInstructor();
        return examRepository.findByInstructor_InstructorId(instructor.getInstructorId())
                .stream()
                .map(examMapper::toExamOutputDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentOutputDTO> getEnrolledStudents(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course not found with id: " + courseId));

        Instructor instructor = getLoggedInInstructor();
        boolean isAssigned = courseRepository
                .findByCourseIdAndInstructor_InstructorId(courseId, instructor.getInstructorId())
                .isPresent();

        if (!isAssigned) {
            throw new AccessDeniedException("Access denied. This course is not assigned to you.");
        }

        return enrollmentRepository.findByCourse_CourseId(courseId)
                .stream()
                .map(studentMapper::toEnrollmentOutputDTO)
                .collect(Collectors.toList());
    }
}