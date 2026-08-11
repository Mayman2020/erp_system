package com.erp.system.common.security;

import com.erp.system.auth.service.AccessControlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuActionAuthorizationFilterTest {

    @Mock
    private AccessControlService accessControlService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private MenuActionAuthorizationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new MenuActionAuthorizationFilter(accessControlService, new ObjectMapper());
        SecurityContextHolder.clearContext();
    }

    @Test
    void servletPathUnderContextPathMapsToInventoryMenu() {
        when(request.getServletPath()).thenReturn("/inventory/products");
        assertEquals("erp-inventory-products", filter.resolveMenuItemId(filter.normalizedPath(request)));
    }

    @Test
    void requestUriFallbackStripsApiV1Prefix() {
        when(request.getServletPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/v1/sales/invoices/12/approve");
        assertEquals("erp-sales-invoices", filter.resolveMenuItemId(filter.normalizedPath(request)));
    }

    @Test
    void prefixBoundaryDoesNotMatchExtraSuffix() {
        assertNull(filter.resolveMenuItemId("/inventory/products-extra"));
    }

    @Test
    void deniesCreateWithoutPermission() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/inventory/products");
        when(response.getOutputStream()).thenReturn(new jakarta.servlet.ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(jakarta.servlet.WriteListener writeListener) {}
            @Override public void write(int b) {}
        });

        JwtPrincipal principal = new JwtPrincipal(42L, "cashier");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_CASHIER"))));
        when(accessControlService.hasMenuAction(any(), eq("erp-inventory-products"), eq("CREATE")))
                .thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void allowsAdminBypass() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getServletPath()).thenReturn("/inventory/products");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(accessControlService, never()).hasMenuAction(any(), any(), any());
    }
}
