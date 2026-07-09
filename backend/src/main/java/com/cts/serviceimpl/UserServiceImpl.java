package com.cts.serviceimpl;

import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.annotation.AuditEvent;
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumerate.Role;
import com.cts.exception.*;
import com.cts.repository.*;
import com.cts.service.UserService;
import com.cts.util.JwtUtil;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InstructorRepository instructorRepository;
    private final StudentRepository studentRepository;
    private final RegistrarRepository registrarRepository;
    private final ExamCoordinatorRepository examCoordinatorRepository;
    private final JwtUtil jwtUtil;

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // REGISTER
    @Override
    @Transactional
    @AuditEvent(
            eventName    = "USER_REGISTERED",
            eventType    = "CREATE",
            eventMessage = "A new user was registered"
    )
    public RegistrationOutputDTO addUser(RegistrationInputDTO registrationInputDTO) {

        if (!isValidEmail(registrationInputDTO.getEmail())) {
            throw new InvalidEmailException("Invalid email format");
        }
        if (userRepository.existsByEmail(registrationInputDTO.getEmail())) {
            throw new InvalidEmailException("Email is already registered!");
        }

        Role verifiedRole;
        try {
            verifiedRole = Role.valueOf(
                    registrationInputDTO.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidEmailException(
                    "Invalid Role. Must be one of: STUDENT, INSTRUCTOR, " +
                            "REGISTRAR, EXAM_COORDINATOR");
        }

        User user = User.builder()
                .email(registrationInputDTO.getEmail().trim())
                .name(registrationInputDTO.getName())
                .password(passwordEncoder.encode(registrationInputDTO.getPassword()))
                .role(verifiedRole)
                .phone(registrationInputDTO.getPhone())
                .status("ACTIVE") // Ensure status is assigned explicitly upon persistence
                .build();

        User savedUser = userRepository.save(user);

        switch (verifiedRole) {
            case INSTRUCTOR -> instructorRepository.save(
                    Instructor.builder()
                            .user(savedUser)
                            .status("ACTIVE")
                            .build());
            case STUDENT -> studentRepository.save(
                    Student.builder()
                            .user(savedUser)
                            .status("ACTIVE")
                            .build());
            case REGISTRAR -> registrarRepository.save(
                    Registrar.builder()
                            .user(savedUser)
                            .build());
            case EXAM_COORDINATOR -> examCoordinatorRepository.save(
                    ExamCoordinator.builder()
                            .user(savedUser)
                            .build());
        }

        return RegistrationOutputDTO.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role(savedUser.getRole())
                .phone(savedUser.getPhone())
                .status(savedUser.getStatus())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    // LOGIN
    @Override
    @AuditEvent(
            eventName    = "USER_LOGGED_IN",
            eventType    = "AUTH",
            eventMessage = "A user successfully logged in"
    )
    public LoginResponseDTO userLogin(LoginDTO loginDTO) {

        if (!isValidEmail(loginDTO.getEmail())) {
            throw new InvalidEmailException("Invalid email format");
        }

        User user = userRepository.findByEmail(loginDTO.getEmail());
        if (user == null) {
            // FIXED: Intercepts bad credentials with dedicated custom exception
            throw new InvalidCredentialsException("Invalid credentials provided.");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            // FIXED: Intercepts bad credentials with dedicated custom exception
            throw new InvalidCredentialsException("Invalid credentials provided.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BusinessException("User account is not active.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return LoginResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .userName(user.getName())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .token(token)
                .build();
    }

    // GET ROLE IDENTITY
    @Override
    @AuditEvent(
            eventName    = "ROLE_IDENTITY_FETCHED",
            eventType    = "READ",
            eventMessage = "Role identity fetched by email"
    )
    public RoleIdentityDTO getRoleIdentity(String role, String email) {

        if (!isValidEmail(email)) {
            throw new InvalidEmailException("Invalid email format");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }

        return switch (role.toLowerCase()) {
            case "registrar" -> {
                Registrar r = registrarRepository.findByUserEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Registrar not found for email: " + email));
                yield RoleIdentityDTO.builder()
                        .roleId(r.getRegistrarId())
                        .userId(r.getUser().getUserId())
                        .email(email)
                        .build();
            }
            case "student" -> {
                Student s = studentRepository.findByUser_Email(email)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Student not found for email: " + email));
                yield RoleIdentityDTO.builder()
                        .roleId(s.getStudentId())
                        .userId(s.getUser().getUserId())
                        .email(email)
                        .build();
            }
            case "instructor" -> {
                Instructor i = instructorRepository.findByUser_Email(email)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Instructor not found for email: " + email));
                yield RoleIdentityDTO.builder()
                        .roleId(i.getInstructorId())
                        .userId(i.getUser().getUserId())
                        .email(email)
                        .build();
            }
            case "exam-coordinator" -> {
                ExamCoordinator ec = examCoordinatorRepository.findByUser_Email(email)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Exam Coordinator not found for email: " + email));
                yield RoleIdentityDTO.builder()
                        .roleId(ec.getCoordinatorId())
                        .userId(ec.getUser().getUserId())
                        .email(email)
                        .build();
            }
            default -> throw new BusinessException(
                    "Invalid role '" + role + "'. Valid roles: registrar, student, instructor, exam-coordinator");
        };
    }
}