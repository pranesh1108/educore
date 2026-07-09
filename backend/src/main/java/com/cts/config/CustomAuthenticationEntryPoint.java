package com.cts.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


//  Handles 401 UNAUTHORIZED — fired when a request arrives with NO valid JWT
//  (missing token, expired token, or malformed token) and tries to access
//  any secured endpoint.
//
//  This is the companion to CustomAccessDeniedHandler:
//    401 = not authenticated at all  (no/bad token)
//    403 = authenticated but wrong role
//
//  Also wired directly into the security filter chain for the same reason —
//  Spring Security throws AuthenticationException before the request reaches
//  any controller or @ExceptionHandler.

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message",
                "Authentication required. Please login via POST /api/v1/user/login "
                + "and include the returned JWT as: Authorization: Bearer <token>");
        body.put("path", request.getRequestURI());
        body.put("timestamp", LocalDateTime.now().toString());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
