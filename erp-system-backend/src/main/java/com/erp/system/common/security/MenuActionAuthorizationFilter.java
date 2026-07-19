package com.erp.system.common.security;

import com.erp.system.auth.service.AccessControlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.erp.system.common.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Enforces {@code role_menu_permissions} for users with custom role assignments on mutating API calls.
 */
@Component
@RequiredArgsConstructor
public class MenuActionAuthorizationFilter extends OncePerRequestFilter {

    private static final List<String> MUTATING_METHODS = List.of("POST", "PUT", "PATCH", "DELETE");

    private static final Map<String, String> PATH_PREFIX_TO_MENU = buildPathMenuMap();

    private final AccessControlService accessControlService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!MUTATING_METHODS.contains(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (path == null) {
            return true;
        }
        return path.startsWith("/auth/")
                || path.startsWith("/admin/")
                || path.startsWith("/health")
                || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isAdmin(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = currentUserId(authentication);
        if (userId == null || !accessControlService.hasCustomAssignments(userId)) {
            filterChain.doFilter(request, response);
            return;
        }

        String menuItemId = resolveMenuItemId(request.getRequestURI());
        if (menuItemId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String action = resolveAction(request.getMethod(), request.getRequestURI());
        if (!accessControlService.hasMenuAction(authentication, menuItemId, action)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(),
                    ApiResponse.error("Insufficient permission for this action"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(code -> "ROLE_ADMIN".equals(code) || "ADMIN".equals(code));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof JwtPrincipal principal) {
            return principal.userId();
        }
        return null;
    }

    private String resolveMenuItemId(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalized = path.startsWith("/api") ? path.substring(4) : path;
        return PATH_PREFIX_TO_MENU.entrySet().stream()
                .filter(entry -> normalized.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String resolveAction(String method, String path) {
        String normalizedPath = path == null ? "" : path.toLowerCase(Locale.ROOT);
        if (HttpMethod.DELETE.matches(method)) {
            return "DELETE";
        }
        if (normalizedPath.contains("/approve")
                || normalizedPath.contains("/cancel")
                || normalizedPath.contains("/deposit")
                || normalizedPath.contains("/clear")
                || normalizedPath.contains("/bounce")
                || normalizedPath.contains("/post")
                || normalizedPath.contains("/reverse")
                || normalizedPath.contains("/finalize")
                || normalizedPath.contains("/status")
                || normalizedPath.contains("/start")
                || normalizedPath.contains("/complete")
                || normalizedPath.contains("/convert")) {
            return "EDIT";
        }
        if (HttpMethod.POST.matches(method)) {
            return "CREATE";
        }
        return "EDIT";
    }

    private static Map<String, String> buildPathMenuMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("/accounting/accounts", "accounts");
        map.put("/accounting/journal-entries", "journal-entries");
        map.put("/accounting/payment-vouchers", "payment-vouchers");
        map.put("/accounting/receipt-vouchers", "receipt-vouchers");
        map.put("/accounting/transfers", "transfers");
        map.put("/accounting/transactions", "transactions");
        map.put("/accounting/invoices", "invoices");
        map.put("/accounting/bills", "bills");
        map.put("/accounting/checks", "checks");
        map.put("/accounting/bank-accounts", "bank-accounts");
        map.put("/accounting/budget", "budget");
        map.put("/accounting/exchange-rates", "settings");
        map.put("/accounting/reconciliation", "reconciliation");
        map.put("/accounting/settings", "settings");
        map.put("/inventory/products", "erp-inventory-products");
        map.put("/inventory/categories", "erp-inventory-categories");
        map.put("/inventory/warehouses", "erp-inventory-warehouses");
        map.put("/inventory/units", "erp-inventory-units");
        map.put("/inventory/stock/movements", "erp-inventory-movements");
        map.put("/inventory/stock/in", "erp-inventory-movements");
        map.put("/inventory/stock/out", "erp-inventory-movements");
        map.put("/inventory/stock/transfer", "erp-inventory-movements");
        map.put("/sales/customers", "erp-sales-customers");
        map.put("/sales/quotations", "erp-sales-quotations");
        map.put("/sales/orders", "erp-sales-orders");
        map.put("/sales/invoices", "erp-sales-invoices");
        map.put("/sales/returns", "erp-sales-returns");
        map.put("/purchases/suppliers", "erp-purchases-suppliers");
        map.put("/purchases/orders", "erp-purchases-orders");
        map.put("/purchases/invoices", "erp-purchases-invoices");
        map.put("/purchases/returns", "erp-purchases-returns");
        map.put("/purchases/payments", "erp-purchases-payments");
        map.put("/hr/departments", "erp-hr-departments");
        map.put("/hr/employees", "erp-hr-employees");
        map.put("/hr/attendance", "erp-hr-attendance");
        map.put("/hr/leave-requests", "erp-hr-leave");
        map.put("/hr/payroll", "erp-hr-payroll");
        map.put("/hr/payroll-lines", "erp-hr-payroll");
        map.put("/hr/documents", "erp-hr-documents");
        map.put("/crm/leads", "erp-crm-leads");
        map.put("/crm/activities", "erp-crm-activities");
        map.put("/crm/notes", "erp-crm-notes");
        map.put("/projects", "erp-projects-list");
        map.put("/manufacturing/work-orders", "erp-manufacturing-orders");
        map.put("/manufacturing/bom", "erp-manufacturing-bom");
        return map;
    }
}
