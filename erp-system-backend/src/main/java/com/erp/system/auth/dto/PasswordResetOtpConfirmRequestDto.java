package com.erp.system.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetOtpConfirmRequestDto {
    @NotBlank(message = "AUTH.EMAIL_REQUIRED")
    @Email(message = "AUTH.EMAIL_INVALID")
    private String email;

    @NotBlank(message = "AUTH.OTP_REQUIRED")
    private String otpCode;

    @NotBlank(message = "AUTH.PASSWORD_REQUIRED")
    @Size(min = 8, max = 120, message = "AUTH.PASSWORD_LENGTH")
    private String newPassword;
}
