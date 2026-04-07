package com.erp.system.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps Railway/Heroku {@code DATABASE_URL} ({@code postgresql://...}) to Spring JDBC properties
 * when {@code DB_URL} / {@code SPRING_DATASOURCE_URL} are not set.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE_NAME = "databaseUrlConverted";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getPropertySources().contains(SOURCE_NAME)) {
            return;
        }
        if (StringUtils.hasText(environment.getProperty("DB_URL"))
                || StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_URL"))) {
            return;
        }
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (!StringUtils.hasText(databaseUrl) || !databaseUrl.startsWith("postgres")) {
            return;
        }

        try {
            URI uri = URI.create(databaseUrl);
            String userInfo = uri.getUserInfo();
            String user = "postgres";
            String pass = "";
            if (StringUtils.hasText(userInfo)) {
                int colon = userInfo.indexOf(':');
                if (colon >= 0) {
                    user = decode(userInfo.substring(0, colon));
                    pass = decode(userInfo.substring(colon + 1));
                } else {
                    user = decode(userInfo);
                }
            }
            String host = uri.getHost() != null ? uri.getHost() : "localhost";
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            String db = StringUtils.hasText(path) && path.length() > 1 ? path.substring(1) : "postgres";

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            if (StringUtils.hasText(uri.getQuery())) {
                jdbcUrl += "?" + uri.getQuery();
            }

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            if (!StringUtils.hasText(environment.getProperty("DB_USER"))
                    && !StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_USERNAME"))) {
                props.put("spring.datasource.username", user);
            }
            if (!StringUtils.hasText(environment.getProperty("DB_PASS"))
                    && !StringUtils.hasText(environment.getProperty("SPRING_DATASOURCE_PASSWORD"))) {
                props.put("spring.datasource.password", pass);
            }
            environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, props));
        } catch (RuntimeException ignored) {
            // Leave configuration as-is; startup will fail with a clear datasource error if invalid
        }
    }

    private static String decode(String s) {
        return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
