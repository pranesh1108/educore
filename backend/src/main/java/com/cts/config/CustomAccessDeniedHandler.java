package com.cts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

//
//  Handles 403 FORBIDDEN — fired when a user IS authenticated (valid JWT)
//
//  Spring Security throws AccessDeniedException internally before the request
//  ever reaches a controller, so @ExceptionHandler in GlobalExceptionHandler
//  cannot catch it. This handler is wired directly into the security filter chain.
//
@Component
public class CustomAccessDeniedHandler
        implements org.springframework.security.web.access.AccessDeniedHandler {

    // Reuse ObjectMapper for JSON serialization (thread-safe singleton)
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        // Pull the role from the SecurityContext so we can include it in the message
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority()   // e.g. "ROLE_STUDENT"
                        .replace("ROLE_", "")                               // → "STUDENT"
                : "UNKNOWN";

        // Build a structured JSON error body
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 403);
        body.put("error", "Forbidden");
        body.put("message", String.format(
                "Access denied. Your role '%s' is not authorized to access this resource: %s",
                role, request.getRequestURI()));
        body.put("path", request.getRequestURI());
        body.put("timestamp", LocalDateTime.now().toString());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
