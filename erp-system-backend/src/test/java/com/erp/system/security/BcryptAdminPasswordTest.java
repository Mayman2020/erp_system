package com.erp.system.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Ensures the Flyway V11 admin password hash matches the documented dev password.
 */
class BcryptAdminPasswordTest {

    @Test
    void adminDevPasswordMatchesMigrationHash() {
        var encoder = new BCryptPasswordEncoder();
        String hash = "$2b$10$6CWuV2VRnCMQwvRzQE6LQu7SAHTflMSv6IvQbUfJhE4y.GktFGXiW";
        Assertions.assertTrue(encoder.matches("Admin@123", hash));
    }
}
