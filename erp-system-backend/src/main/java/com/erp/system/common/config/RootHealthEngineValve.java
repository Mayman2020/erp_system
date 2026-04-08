package com.erp.system.common.config;

import java.io.IOException;
import jakarta.servlet.ServletException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.springframework.http.HttpStatus;

/**
 * Answers {@code GET /health} at the Tomcat engine level (outside the servlet context-path).
 * Load balancers and platforms often probe {@code /health} while this app uses {@code /api/v1} as context path.
 */
final class RootHealthEngineValve extends ValveBase {

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            getNext().invoke(request, response);
            return;
        }
        String uri = request.getRequestURI();
        if (!"/health".equals(uri) && !"/health/".equals(uri)) {
            getNext().invoke(request, response);
            return;
        }
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":\"UP\"}");
    }
}
