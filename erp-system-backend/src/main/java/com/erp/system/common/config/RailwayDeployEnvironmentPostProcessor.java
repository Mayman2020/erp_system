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
        String dbUrl = environment.getProperty("DB_URL");
        if (StringUtils.hasText(dbUrl) && !containsUnresolvedRailwayPgPlaceholder(dbUrl)) {
            return true;
        }
        String springDsUrl = environment.getProperty("SPRING_DATASOURCE_URL");
        if (StringUtils.hasText(springDsUrl) && !containsUnresolvedRailwayPgPlaceholder(springDsUrl)) {
            return true;
        }
        // Railway Postgres plugin provides concrete PG* values even when SPRING_DATASOURCE_URL is broken.
        if (StringUtils.hasText(System.getenv("PGHOST"))
                && StringUtils.hasText(System.getenv("PGPORT"))
                && StringUtils.hasText(System.getenv("PGDATABASE"))) {
            return true;
        }
        String databaseUrl = environment.getProperty("DATABASE_URL");
        return StringUtils.hasText(databaseUrl) && databaseUrl.startsWith("postgres");
    }

    private static boolean containsUnresolvedRailwayPgPlaceholder(String value) {
        return value.contains("${PGHOST")
                || value.contains("${PGPORT")
                || value.contains("${PGDATABASE")
                || value.contains("${PGUSER")
                || value.contains("${PGPASSWORD");
    }
}
