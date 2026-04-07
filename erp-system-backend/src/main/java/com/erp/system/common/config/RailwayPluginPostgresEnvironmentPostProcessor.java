package com.erp.system.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Railway Postgres plugin sets {@code PGHOST}, {@code PGPORT}, {@code PGDATABASE}, {@code PGUSER},
 * {@code PGPASSWORD} as real values. If you set
 * {@code SPRING_DATASOURCE_URL=jdbc:postgresql://${PGHOST}:...} in the Railway UI, that string is
 * passed through <strong>without</strong> substituting {@code ${PGHOST}} — Spring then sees an
 * invalid JDBC URL. This processor overrides datasource properties using the resolved PG_* env vars.
 */
public class RailwayPluginPostgresEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE_NAME = "railwayPluginPostgresJdbc";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getPropertySources().contains(SOURCE_NAME)) {
            return;
        }
        String host = System.getenv("PGHOST");
        String port = System.getenv("PGPORT");
        String database = System.getenv("PGDATABASE");
        if (!StringUtils.hasText(host) || !StringUtils.hasText(port) || !StringUtils.hasText(database)) {
            return;
        }

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("spring.datasource.url", buildJdbcUrl(host, port, database));
        String user = System.getenv("PGUSER");
        String pass = System.getenv("PGPASSWORD");
        if (StringUtils.hasText(user)) {
            props.put("spring.datasource.username", user);
        }
        if (StringUtils.hasText(pass)) {
            props.put("spring.datasource.password", pass);
        }
        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, props));
    }

    private static String buildJdbcUrl(String host, String port, String database) {
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        String extra = System.getenv("PG_JDBC_EXTRA");
        if (!StringUtils.hasText(extra)) {
            return url;
        }
        String e = extra.trim();
        if (e.startsWith("?")) {
            return url + e;
        }
        return url + "?" + e;
    }
}
