package com.erp.system.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Fail-fast production secret validation at bean init (before traffic is accepted).
 */
@Configuration
@Profile("prod")
public class ErpProductionSecretsValidator {

    private static final Set<String> FORBIDDEN_JWT_SECRETS = Set.of(
            "erp-system-super-secret-key-change-me-2026",
            "change-me",
            "secret",
            "jwt-secret"
    );

    private final Environment environment;

    @Value("${app.security.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.cors.allow-all-origins:false}")
    private boolean allowAllOrigins;

    public ErpProductionSecretsValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateRequiredSecrets() {
        requireEnv("JWT_SECRET");
        requireEnv("SPRING_DATASOURCE_PASSWORD");

        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("Production requires app.security.jwt.secret / JWT_SECRET");
        }
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes in production");
        }
        if (FORBIDDEN_JWT_SECRETS.contains(jwtSecret.trim().toLowerCase())) {
            throw new IllegalStateException("JWT_SECRET must not use a known development default in production");
        }
        if (allowAllOrigins) {
            throw new IllegalStateException("CORS_ALLOW_ALL=true is forbidden in production");
        }
    }

    private void requireEnv(String key) {
        String value = environment.getProperty(key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Production requires environment variable: " + key);
        }
    }
}
