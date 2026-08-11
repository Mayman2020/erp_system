package com.erp.system.common.security;

import com.erp.system.auth.service.AccessControlService;
import com.erp.system.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ActiveRoleFilter extends OncePerRequestFilter {

    public static final String ACTIVE_ROLE_HEADER = "X-Active-Role";

    private final AccessControlService accessControlService;
    private final ActiveRoleContext activeRoleContext;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestedRole = request.getHeader(ACTIVE_ROLE_HEADER);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!StringUtils.hasText(requestedRole) || authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String normalizedRole = requestedRole.trim().toUpperCase(Locale.ROOT);
        if (!accessControlService.isAssignedRole(authentication, normalizedRole)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), ApiResponse.error("Invalid active role"));
            return;
        }

        activeRoleContext.setRoleCode(normalizedRole);
        Authentication narrowed = new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(),
                authentication.getCredentials(),
                List.of(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
        );
        SecurityContextHolder.getContext().setAuthentication(narrowed);
        filterChain.doFilter(request, response);
    }
}
