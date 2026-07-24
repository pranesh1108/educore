package com.cts.util;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static String getLoggedInEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}