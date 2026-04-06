package com.erp.system.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequestDto {

    @NotBlank(message = "PROFILE.USERNAME_REQUIRED")
    @Size(min = 4, max = 100, message = "PROFILE.USERNAME_LENGTH")
    private String username;

    @NotBlank(message = "PROFILE.EMAIL_REQUIRED")
    @Email(message = "PROFILE.EMAIL_INVALID")
    @Size(max = 190, message = "PROFILE.EMAIL_LENGTH")
    private String email;

    @NotBlank(message = "PROFILE.PHONE_REQUIRED")
    @Size(max = 30, message = "PROFILE.PHONE_LENGTH")
    private String phone;

    @NotBlank(message = "PROFILE.FULL_NAME_EN_REQUIRED")
    @Size(max = 150, message = "PROFILE.FULL_NAME_EN_LENGTH")
    private String fullNameEn;

    @NotBlank(message = "PROFILE.FULL_NAME_AR_REQUIRED")
    @Size(max = 150, message = "PROFILE.FULL_NAME_AR_LENGTH")
    private String fullNameAr;

    private String profileImage;

    @Size(max = 60, message = "PROFILE.NATIONAL_ID_LENGTH")
    private String nationalId;

    @Size(max = 180, message = "PROFILE.COMPANY_NAME_EN_LENGTH")
    private String companyNameEn;

    @Size(max = 180, message = "PROFILE.COMPANY_NAME_AR_LENGTH")
    private String companyNameAr;
}
