package com.erp.system.common.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
@Profile("prod")
public class ErpProductionSecretsValidator {

    private final Environment environment;

    public ErpProductionSecretsValidator(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateRequiredSecrets() {
        require("JWT_SECRET");
        require("SPRING_DATASOURCE_PASSWORD");
    }

    private void require(String key) {
        String value = environment.getProperty(key);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("Production requires environment variable: " + key);
        }
    }
}
