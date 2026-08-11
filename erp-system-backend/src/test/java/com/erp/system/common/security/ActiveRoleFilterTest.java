package com.erp.system.common.security;

import com.erp.system.auth.service.AccessControlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActiveRoleFilterTest {

    @Mock private AccessControlService accessControlService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private ActiveRoleFilter filter;
    private ActiveRoleContext context;

    @BeforeEach
    void setUp() {
        context = new ActiveRoleContext();
        filter = new ActiveRoleFilter(accessControlService, context, new ObjectMapper());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new JwtPrincipal(7L, "multi"),
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_REPORT_VIEWER")
                )
        ));
    }

    @Test
    void ignoresMissingHeader() throws Exception {
        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(accessControlService, never()).isAssignedRole(any(), any());
    }

    @Test
    void deniesUnassignedHeader() throws Exception {
        when(request.getHeader(ActiveRoleFilter.ACTIVE_ROLE_HEADER)).thenReturn("HR");
        when(accessControlService.isAssignedRole(any(), any())).thenReturn(false);
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) {}
            @Override public void write(int value) {}
        });

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void validHeaderNarrowsAuthenticationToOneRole() throws Exception {
        when(request.getHeader(ActiveRoleFilter.ACTIVE_ROLE_HEADER)).thenReturn("report_viewer");
        when(accessControlService.isAssignedRole(any(), any())).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertEquals("REPORT_VIEWER", context.getRoleCode());
        assertEquals(
                List.of("ROLE_REPORT_VIEWER"),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList()
        );
        verify(filterChain).doFilter(request, response);
    }
}
