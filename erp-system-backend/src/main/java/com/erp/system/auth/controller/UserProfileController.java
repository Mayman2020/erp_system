package com.erp.system.auth.controller;

import com.erp.system.auth.dto.AuthUserDto;
import com.erp.system.auth.dto.UpdateProfileRequestDto;
import com.erp.system.auth.service.UserProfileService;
import com.erp.system.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ApiResponse<AuthUserDto> getMyProfile() {
        return ApiResponse.success(userProfileService.getMyProfile());
    }

    @PutMapping("/me")
    public ApiResponse<AuthUserDto> updateMyProfile(@Valid @RequestBody UpdateProfileRequestDto request) {
        return ApiResponse.success(userProfileService.updateMyProfile(request));
    }
}
