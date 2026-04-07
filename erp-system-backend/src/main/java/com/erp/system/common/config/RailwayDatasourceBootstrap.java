package com.erp.system.common.config;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Runs before {@link org.springframework.boot.SpringApplication#run} so {@code spring.datasource.*}
 * <strong>system properties</strong> override broken Railway env values.
 * <p>
 * Railway often sets {@code SPRING_DATASOURCE_URL=jdbc:postgresql://${PGHOST}:...} as a literal string
 * (no interpolation). That binds directly to {@code spring.datasource.url} and can win over
 * {@link EnvironmentPostProcessor} sources. Setting system properties here fixes startup reliably.
 */
public final class RailwayDatasourceBootstrap {

    private RailwayDatasourceBootstrap() {}

    public static void applyIfNeeded() {
        applyFromPgPluginVars();
        applyFromDatabaseUrlWhenSpringDatasourceBroken();
    }

    private static void applyFromPgPluginVars() {
        String host = System.getenv("PGHOST");
        String port = System.getenv("PGPORT");
        String database = System.getenv("PGDATABASE");
        if (!isSet(host) || !isSet(port) || !isSet(database)) {
            return;
        }
        System.setProperty("spring.datasource.url", "jdbc:postgresql://" + host + ":" + port + "/" + database);
        String user = System.getenv("PGUSER");
        String pass = System.getenv("PGPASSWORD");
        if (isSet(user)) {
            System.setProperty("spring.datasource.username", user);
        }
        if (isSet(pass)) {
            System.setProperty("spring.datasource.password", pass);
        }
    }

    private static void applyFromDatabaseUrlWhenSpringDatasourceBroken() {
        String springDs = System.getenv("SPRING_DATASOURCE_URL");
        if (!isSet(springDs) || !springDs.contains("${PGHOST")) {
            return;
        }
        String databaseUrl = System.getenv("DATABASE_URL");
        if (!isSet(databaseUrl) || !databaseUrl.startsWith("postgres")) {
            return;
        }
        try {
            URI uri = URI.create(databaseUrl);
            String userInfo = uri.getUserInfo();
            String user = "postgres";
            String pass = "";
            if (isSet(userInfo)) {
                int colon = userInfo.indexOf(':');
                if (colon >= 0) {
                    user = decode(userInfo.substring(0, colon));
                    pass = decode(userInfo.substring(colon + 1));
                } else {
                    user = decode(userInfo);
                }
            }
            String host = uri.getHost() != null ? uri.getHost() : "localhost";
            int uriPort = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            String db = isSet(path) && path.length() > 1 ? path.substring(1) : "postgres";
            String jdbcUrl = "jdbc:postgresql://" + host + ":" + uriPort + "/" + db;
            if (isSet(uri.getQuery())) {
                jdbcUrl += "?" + uri.getQuery();
            }
            System.setProperty("spring.datasource.url", jdbcUrl);
            System.setProperty("spring.datasource.username", user);
            System.setProperty("spring.datasource.password", pass);
        } catch (RuntimeException ignored) {
            // keep env as-is
        }
    }

    private static boolean isSet(String s) {
        return s != null && !s.isBlank();
    }

    private static String decode(String s) {
        return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
