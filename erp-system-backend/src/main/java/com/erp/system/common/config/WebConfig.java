package com.erp.system.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {

    private static final List<String> ALLOWED_METHODS =
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD");
    private static final List<String> ALLOWED_HEADERS = List.of("*");
    /** Let browsers read JWT and filenames from cross-origin responses when credentials are used. */
    private static final List<String> EXPOSED_HEADERS =
            List.of("Authorization", "Content-Type", "Content-Disposition");

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allow-all-origins:false}") boolean allowAllOrigins,
            @Value("${app.cors.allowed-origins}") String allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(ALLOWED_METHODS);
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        configuration.setExposedHeaders(EXPOSED_HEADERS);
        configuration.setMaxAge(3600L);

        if (allowAllOrigins) {
            configuration.setAllowedOriginPatterns(List.of("*"));
            // Wildcard origin is incompatible with credentials in browsers.
            configuration.setAllowCredentials(false);
        } else {
            configuration.setAllowedOriginPatterns(Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .toList());
            configuration.setAllowCredentials(true);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
