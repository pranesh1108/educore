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

        // ── STRICT BUSINESS CONSTRAINT: ONLY ONE REGISTRAR/ADMIN ALLOWED ──
        if (verifiedRole == Role.REGISTRAR) {
            boolean registrarExists = userRepository.existsByRole(Role.REGISTRAR);
            if (registrarExists) {
                throw new BusinessException("Registration Denied: A Registrar account already exists in the system.");
            }
        }

        User user = User.builder()
                .email(registrationInputDTO.getEmail().trim())
                .name(registrationInputDTO.getName())
                .password(passwordEncoder.encode(registrationInputDTO.getPassword()))
                .role(verifiedRole)
                .phone(registrationInputDTO.getPhone())
                .build();

        User savedUser = userRepository.save(user);

        switch (verifiedRole) {
            case INSTRUCTOR -> instructorRepository.save(
                    Instructor.builder()
                            .user(savedUser)
                            .build());
            case STUDENT -> studentRepository.save(
                    Student.builder()
                            .user(savedUser)
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
                .createdAt(savedUser.getCreatedAt())
                .build();
    }


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
            throw new InvalidCredentialsException("Invalid credentials provided.");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials provided.");
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
}