package com.cts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;


// Swagger at: http://localhost:9098/swagger-ui/index.html

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI eduCore360OpenAPI() {
        return new OpenAPI()

            .addServersItem(new Server()
                .url("http://localhost:9098")
                .description("Local Development Server"))

            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description(
                        "Obtain your JWT from POST /api/v1/user/login, "
                        + "then paste it here (without the 'Bearer ' prefix)."
                    )))


            .info(new Info()
                .title("EduCore360 API")
                .version("1.0.0")
                .description("""
                    EduCore360 is a role-based academic management platform.

                    Roles and their base paths:
                      • STUDENT          → /api/v1/student/**
                      • INSTRUCTOR       → /api/v1/instructor/**
                      • REGISTRAR        → /api/v1/registrar/**
                      • EXAM_COORDINATOR → /api/v1/exam-coordinator/**

                    Public endpoints (no token needed):
                      • POST /api/v1/user/register
                      • POST /api/v1/user/login
                    """)
                .contact(new Contact()
                    .name("Development Team")
                    .email("support@educore360.com"))
                .license(new License()
                    .name("Apache License 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
