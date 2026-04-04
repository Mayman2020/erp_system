package com.erp.system.common.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Resolves the current username from Spring Security (JWT API or form login).
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof JwtPrincipal jwt) {
            return jwt.username();
        }
        if (principal instanceof AppUserPrincipal app) {
            return app.getUsername();
        }
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        if (principal instanceof String s && !s.isBlank()) {
            return s;
        }
        return null;
    }
}
