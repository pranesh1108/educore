package com.cts.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    // Extracts the email of the currently logged-in user from the JWT token.
    // JWTFilter sets username = email when building the Authentication object,
    // so Authentication.getName() always returns the email.
    public static String getLoggedInEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}