package com.erp.system.admin.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LicenseServiceSignatureTest {

    @Test
    void hmacSignatureIsDeterministic() {
        String payload = LicenseService.buildPayload(
                "KEY-1", "Acme", "POS,FINANCE", 25, "2026-01-01", "2027-01-01", 7);
        String a = LicenseService.hmacSha256Hex(payload, "erp-dev-license-signing-secret-32b");
        String b = LicenseService.hmacSha256Hex(payload, "erp-dev-license-signing-secret-32b");
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(64, a.length());
    }
}
