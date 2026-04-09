package com.erp.system;

import com.erp.system.common.config.RailwayDatasourceBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ErpSystemApplication {

    public static void main(String[] args) {
        // Railway: never run the local "dev" profile (localhost Postgres) on the platform.
        if (isRailway() && useDevProfileOnRailway()) {
            System.setProperty("spring.profiles.active", "prod");
        }
        RailwayDatasourceBootstrap.applyIfNeeded();
        SpringApplication.run(ErpSystemApplication.class, args);
    }

    private static boolean isRailway() {
        String r = System.getenv("RAILWAY_ENVIRONMENT");
        return r != null && !r.isBlank();
    }

    /** True when SPRING_PROFILES_ACTIVE is unset or explicitly "dev" (common copy-paste mistake). */
    private static boolean useDevProfileOnRailway() {
        String spa = System.getenv("SPRING_PROFILES_ACTIVE");
        return spa == null || spa.isBlank() || "dev".equalsIgnoreCase(spa.trim());
    }
}
