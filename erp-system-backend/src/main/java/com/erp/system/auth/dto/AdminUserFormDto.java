package com.erp.system.auth.dto;

import com.erp.system.auth.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminUserFormDto {

    @NotBlank
    @Size(min = 4, max = 100)
    private String username;

    @NotBlank
    @Email
    @Size(max = 190)
    private String email;

    @NotBlank
    @Size(max = 30)
    private String phone;

    @Size(min = 8, max = 255)
    @Pattern(
            regexp = "^$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$",
            message = "AUTH.REGISTER.PASSWORD_WEAK"
    )
    private String password;

    @NotBlank
    @Size(max = 150)
    private String fullNameEn;

    @NotBlank
    @Size(max = 150)
    private String fullNameAr;

    @NotNull
    private UserRole primaryRole;

    private Boolean active = true;

    private List<Long> roleIds = new ArrayList<>();
}
