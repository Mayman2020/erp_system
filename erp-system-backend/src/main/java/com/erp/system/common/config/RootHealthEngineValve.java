package com.erp.system.common.config;

import java.io.IOException;
import jakarta.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.http.HttpStatus;

/**
 * Answers {@code GET /health} and {@code GET /actuator/health} at the Tomcat engine level (outside the servlet context-path).
 * Platforms often probe those paths while the API uses context path {@code /api/v1}.
 */
final class RootHealthEngineValve extends ValveBase {

    private static boolean isRootHealthUri(String uri) {
        return "/health".equals(uri)
                || "/health/".equals(uri)
                || "/actuator/health".equals(uri)
                || "/actuator/health/".equals(uri);
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            getNext().invoke(request, response);
            return;
        }
        String uri = request.getRequestURI();
        if (!isRootHealthUri(uri)) {
            getNext().invoke(request, response);
            return;
        }
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":\"UP\"}");
    }
}
