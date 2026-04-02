package com.erp.system.auth.repository;

import com.erp.system.auth.domain.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findFirstByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(String email);
}
