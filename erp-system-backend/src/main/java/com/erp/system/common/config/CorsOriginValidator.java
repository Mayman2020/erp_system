package com.erp.system.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CorsOriginValidator implements InitializingBean {

    private final Environment environment;

    @Override
    public void afterPropertiesSet() {
        if (!isProdProfile()) {
            return;
        }
        boolean allowAll = Boolean.parseBoolean(environment.getProperty("app.cors.allow-all-origins", "false"));
        if (allowAll) {
            return;
        }
        String origins = environment.getProperty("app.cors.allowed-origins", "");
        if (origins == null || origins.isBlank()) {
            throw new IllegalStateException(
                    "CORS_ALLOWED_ORIGINS is required when spring.profiles.active includes 'prod' and CORS_ALLOW_ALL is false. "
                            + "Set comma-separated origin patterns, e.g. https://erp.example.com");
        }
    }

    private boolean isProdProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
