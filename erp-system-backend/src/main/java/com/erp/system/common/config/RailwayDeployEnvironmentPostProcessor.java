package com.erp.system.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

/**
 * Railway: default to {@code prod} when {@code SPRING_PROFILES_ACTIVE} is unset, then
 * add {@code h2demo} if no external JDBC URL is configured (in-memory demo DB).
 */
public class RailwayDeployEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!StringUtils.hasText(System.getenv("RAILWAY_ENVIRONMENT"))) {
            return;
        }
        String spa = System.getenv("SPRING_PROFILES_ACTIVE");
        if (!StringUtils.hasText(spa) || "dev".equalsIgnoreCase(spa.trim())) {
            environment.setActiveProfiles("prod");
        }
        if (hasExternalJdbcConfiguration(environment)) {
            return;
        }
        for (String p : environment.getActiveProfiles()) {
            if ("h2demo".equalsIgnoreCase(p)) {
                return;
            }
        }
        environment.addActiveProfile("h2demo");
    }

    private static boolean hasExternalJdbcConfiguration(ConfigurableEnvironment environment) {
        if (StringUtils.hasText(environment.getProperty("DB_URL"))) {
            return true;
        }
        if (StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_URL"))) {
            return true;
        }
        String databaseUrl = environment.getProperty("DATABASE_URL");
        return StringUtils.hasText(databaseUrl) && databaseUrl.startsWith("postgres");
    }
}
