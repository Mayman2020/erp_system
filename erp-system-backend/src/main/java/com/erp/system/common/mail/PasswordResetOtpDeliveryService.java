package com.erp.system.common.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetOtpDeliveryService {

    private final EmailService emailService;

    public void deliver(String email, String otpCode) {
        if (email == null || email.isBlank() || otpCode == null || otpCode.isBlank()) {
            return;
        }
        String subject = "ERP password reset code";
        String body = """
                A password reset was requested for your ERP account.

                Your one-time code (valid 10 minutes):
                %s

                If you did not request this, ignore this message.
                """.formatted(otpCode.trim());
        boolean sent = emailService.sendOptional(email, subject, body);
        if (!sent) {
            log.info("Password reset OTP for {} (mail off) - code for dev: {}", email, otpCode);
        }
    }
}
