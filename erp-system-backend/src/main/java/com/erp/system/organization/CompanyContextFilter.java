package com.erp.system.organization;

import com.erp.system.common.security.JwtPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CompanyContextFilter extends OncePerRequestFilter {
    public static final String COMPANY_HEADER = "X-Company-Id";
    private final CompanyAccessService companyAccessService;
    private final CompanyContext companyContext;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            Long companyId = companyAccessService.resolveAndValidate(
                    principal.userId(), request.getHeader(COMPANY_HEADER));
            companyContext.setCompanyId(companyId);
            response.setHeader(COMPANY_HEADER, companyId.toString());
            chain.doFilter(request, response);
        } catch (RuntimeException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(),
                    Map.of("success", false, "message", ex.getMessage()));
        } finally {
            companyContext.clear();
        }
    }
}
