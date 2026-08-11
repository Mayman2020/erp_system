package com.erp.system.admin.repository;

import com.erp.system.admin.domain.SystemLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemLicenseRepository extends JpaRepository<SystemLicense, Long> {
    Optional<SystemLicense> findFirstByActiveTrueOrderByActivatedAtDescIdDesc();

    Optional<SystemLicense> findByLicenseKeyIgnoreCase(String licenseKey);
}
