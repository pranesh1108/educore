package com.cts.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;


@Configuration
@AllArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtFilter jwtFilter;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        //  Public  no token required
                        .requestMatchers(
                                "/api/v1/user/register",
                                "/api/v1/user/login"
                        ).permitAll()

                        // Swagger UI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Shared for all roles
                        .requestMatchers("/api/v1/courses/all", "/api/v1/courses/filter").authenticated()

                        // Role identity lookup — any authenticated user
                        .requestMatchers("/api/v1/registrar/{email}").authenticated()
                        .requestMatchers("/api/v1/student/{email}").authenticated()
                        .requestMatchers("/api/v1/instructor/{email}").authenticated()
                        .requestMatchers("/api/v1/exam-coordinator/{email}").authenticated()

                        // Role-specific paths
                        .requestMatchers("/api/v1/registrar/**").hasRole("REGISTRAR")
                        .requestMatchers("/api/v1/instructor/**").hasRole("INSTRUCTOR")
                        .requestMatchers("/api/v1/student/**").hasRole("STUDENT")
                        .requestMatchers("/api/v1/exam-coordinator/**").hasRole("EXAM_COORDINATOR")
                        .requestMatchers("/api/v1/results/**").permitAll()

                        // Catch-all: any unmapped path still requires authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)  // 401 — no/bad token
                        .accessDeniedHandler(accessDeniedHandler)             // 403 — wrong role
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
