package com.erp.system.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Ensures the latest Flyway admin password hash matches the documented dev password.
 */
class BcryptAdminPasswordTest {

    @Test
    void adminDevPasswordMatchesMigrationHash() {
        var encoder = new BCryptPasswordEncoder();
        String hash = "$2b$10$49wu0oR2J3vEOrZkEGsLMuLFpKEt3nrQ9pnquwuvfZu2ceMvriOnq";
        Assertions.assertTrue(encoder.matches("admin", hash));
    }
}
