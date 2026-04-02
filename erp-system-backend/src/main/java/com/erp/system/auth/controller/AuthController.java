package com.erp.system.auth.controller;

import com.erp.system.auth.dto.AuthLoginRequestDto;
import com.erp.system.auth.dto.AuthLoginRolesRequestDto;
import com.erp.system.auth.dto.AuthRegisterRequestDto;
import com.erp.system.auth.dto.AuthResponseDto;
import com.erp.system.auth.dto.PasswordResetOtpConfirmRequestDto;
import com.erp.system.auth.dto.PasswordResetOtpSendRequestDto;
import com.erp.system.auth.dto.RefreshTokenRequestDto;
import com.erp.system.auth.service.AuthService;
import com.erp.system.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponseDto> login(@Valid @RequestBody AuthLoginRequestDto request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/login/roles")
    public ApiResponse<java.util.List<String>> resolveLoginRoles(@Valid @RequestBody AuthLoginRolesRequestDto request) {
        return ApiResponse.success(authService.resolveLoginRoles(request));
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponseDto> register(@Valid @RequestBody AuthRegisterRequestDto request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        return ApiResponse.success(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/password/otp/send")
    public ApiResponse<Boolean> sendPasswordResetOtp(@Valid @RequestBody PasswordResetOtpSendRequestDto request) {
        return ApiResponse.success(authService.sendPasswordResetOtp(request));
    }

    @PostMapping("/password/otp/reset")
    public ApiResponse<Boolean> resetPasswordWithOtp(@Valid @RequestBody PasswordResetOtpConfirmRequestDto request) {
        return ApiResponse.success(authService.resetPasswordWithOtp(request));
    }
}
