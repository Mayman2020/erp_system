package com.erp.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDto {

    @NotBlank(message = "AUTH.REFRESH.TOKEN_REQUIRED")
    private String refreshToken;
}
