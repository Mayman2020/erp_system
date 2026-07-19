package com.erp.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {

    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 255)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$",
            message = "AUTH.REGISTER.PASSWORD_WEAK"
    )
    private String newPassword;
}
