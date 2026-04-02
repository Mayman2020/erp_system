package com.erp.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthLoginRequestDto {

    @NotBlank(message = "AUTH.LOGIN.USERNAME_OR_EMAIL_REQUIRED")
    private String usernameOrEmail;

    @NotBlank(message = "AUTH.LOGIN.PASSWORD_REQUIRED")
    private String password;
}
