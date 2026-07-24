package com.cts.config;

import com.cts.entity.Registrar;
import com.cts.entity.User;
import com.cts.enumerate.Role;
import com.cts.repository.RegistrarRepository;
import com.cts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrarInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RegistrarRepository registrarRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${educore.registrar.email:registrar@educore.com}")
    private String email;

    @Value("${educore.registrar.password:Registrar@123}")
    private String password;

    @Value("${educore.registrar.name:System Registrar}")
    private String name;

    @Value("${educore.registrar.phone:9876543210}")
    private Long phone;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(email)) {
            User registrarUser = User.builder()
                    .email(email.trim())
                    .name(name)
                    .password(passwordEncoder.encode(password))
                    .role(Role.REGISTRAR)
                    .phone(phone)
                    .build();

            User savedUser = userRepository.save(registrarUser);

            Registrar registrar = Registrar.builder()
                    .user(savedUser)
                    .build();

            registrarRepository.save(registrar);
            System.out.println(">>> System Registrar account successfully initialized: " + email);
        }
    }
}