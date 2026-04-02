package com.erp.system.auth.dto;

import com.erp.system.auth.domain.RegistrationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRegisterRequestDto {

    @NotNull(message = "AUTH.REGISTER.TYPE_REQUIRED")
    private RegistrationType registrationType;

    @NotBlank(message = "AUTH.REGISTER.USERNAME_REQUIRED")
    @Size(min = 4, max = 100, message = "AUTH.REGISTER.USERNAME_LENGTH")
    private String username;

    @NotBlank(message = "AUTH.REGISTER.EMAIL_REQUIRED")
    @Email(message = "AUTH.REGISTER.EMAIL_INVALID")
    @Size(max = 190, message = "AUTH.REGISTER.EMAIL_LENGTH")
    private String email;

    @NotBlank(message = "AUTH.REGISTER.PHONE_REQUIRED")
    @Size(max = 30, message = "AUTH.REGISTER.PHONE_LENGTH")
    private String phone;

    @NotBlank(message = "AUTH.REGISTER.PASSWORD_REQUIRED")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$",
            message = "AUTH.REGISTER.PASSWORD_WEAK"
    )
    private String password;

    @NotBlank(message = "AUTH.REGISTER.FULL_NAME_REQUIRED")
    @Size(max = 150, message = "AUTH.REGISTER.FULL_NAME_LENGTH")
    private String fullName;

    @Size(max = 60, message = "AUTH.REGISTER.NATIONAL_ID_LENGTH")
    private String nationalId;

    @Size(max = 180, message = "AUTH.REGISTER.COMPANY_NAME_LENGTH")
    private String companyName;
}
