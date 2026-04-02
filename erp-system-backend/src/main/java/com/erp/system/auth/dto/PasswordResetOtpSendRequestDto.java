package com.erp.system.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetOtpSendRequestDto {
    @NotBlank(message = "AUTH.EMAIL_REQUIRED")
    @Email(message = "AUTH.EMAIL_INVALID")
    private String email;
}
