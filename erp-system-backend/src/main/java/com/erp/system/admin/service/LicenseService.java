package com.erp.system.admin.service;

import com.erp.system.admin.domain.SystemLicense;
import com.erp.system.admin.dto.display.LicenseDisplayDto;
import com.erp.system.admin.dto.form.LicenseActivateFormDto;
import com.erp.system.admin.repository.SystemLicenseRepository;
import com.erp.system.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final SystemLicenseRepository systemLicenseRepository;

    @Value("${app.license.signing-secret:}")
    private String signingSecret;

    @Transactional(readOnly = true)
    public LicenseDisplayDto getCurrent() {
        return systemLicenseRepository.findFirstByActiveTrueOrderByActivatedAtDescIdDesc()
                .map(this::toDisplay)
                .orElse(null);
    }

    @Transactional
    public LicenseDisplayDto activate(LicenseActivateFormDto request) {
        if (!validateSignature(request)) {
            throw new BusinessException("Invalid license signature");
        }
        LocalDate validFrom = LocalDate.parse(request.getValidFrom());
        LocalDate validTo = LocalDate.parse(request.getValidTo());
        if (validTo.isBefore(validFrom)) {
            throw new BusinessException("License valid-to must be on or after valid-from");
        }

        List<SystemLicense> activeLicenses = systemLicenseRepository.findAll().stream()
                .filter(SystemLicense::isActive)
                .toList();
        activeLicenses.forEach(license -> license.setActive(false));
        systemLicenseRepository.saveAll(activeLicenses);

        SystemLicense license = systemLicenseRepository.findByLicenseKeyIgnoreCase(request.getLicenseKey())
                .orElseGet(SystemLicense::new);
        license.setLicenseKey(request.getLicenseKey());
        license.setCustomerName(request.getCustomerName());
        license.setModulesCsv(request.getModulesCsv());
        license.setMaxUsers(request.getMaxUsers() == null ? 10 : request.getMaxUsers());
        license.setValidFrom(validFrom);
        license.setValidTo(validTo);
        license.setGraceDays(request.getGraceDays() == null ? 7 : request.getGraceDays());
        license.setSignature(request.getSignature());
        license.setActive(true);
        license.setActivatedAt(Instant.now());
        license = systemLicenseRepository.save(license);
        return toDisplay(license);
    }

    public boolean validateSignature(LicenseActivateFormDto request) {
        if (signingSecret == null || signingSecret.isBlank()) {
            return false;
        }
        String payload = buildPayload(
                request.getLicenseKey(),
                request.getCustomerName(),
                request.getModulesCsv(),
                request.getMaxUsers() == null ? 10 : request.getMaxUsers(),
                request.getValidFrom(),
                request.getValidTo(),
                request.getGraceDays() == null ? 7 : request.getGraceDays()
        );
        String expected = hmacSha256Hex(payload, signingSecret);
        return expected.equalsIgnoreCase(request.getSignature().trim());
    }

    static String buildPayload(String licenseKey, String customerName, String modulesCsv,
                               int maxUsers, String validFrom, String validTo, int graceDays) {
        return String.join("|",
                nullSafe(licenseKey),
                nullSafe(customerName),
                nullSafe(modulesCsv),
                String.valueOf(maxUsers),
                nullSafe(validFrom),
                nullSafe(validTo),
                String.valueOf(graceDays)
        );
    }

    static String hmacSha256Hex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new BusinessException("Unable to compute license signature");
        }
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value.trim();
    }

    private LicenseDisplayDto toDisplay(SystemLicense license) {
        LocalDate today = LocalDate.now();
        LocalDate graceEnd = license.getValidTo().plusDays(license.getGraceDays());
        boolean valid = !today.isBefore(license.getValidFrom()) && !today.isAfter(graceEnd);
        return LicenseDisplayDto.builder()
                .id(license.getId())
                .licenseKey(license.getLicenseKey())
                .customerName(license.getCustomerName())
                .modulesCsv(license.getModulesCsv())
                .maxUsers(license.getMaxUsers())
                .validFrom(license.getValidFrom())
                .validTo(license.getValidTo())
                .graceDays(license.getGraceDays())
                .active(license.isActive())
                .valid(valid)
                .activatedAt(license.getActivatedAt())
                .build();
    }
}
