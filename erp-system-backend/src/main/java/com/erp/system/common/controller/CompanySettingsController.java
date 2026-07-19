package com.erp.system.common.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.dto.CompanySettingsDto;
import com.erp.system.common.dto.CompanySettingsUpdateDto;
import com.erp.system.common.service.CompanySettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings/company")
@RequiredArgsConstructor
public class CompanySettingsController {

    private final CompanySettingsService companySettingsService;

    @GetMapping
    public ApiResponse<CompanySettingsDto> getSettings() {
        return ApiResponse.success(companySettingsService.getSettings());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CompanySettingsDto> updateSettings(@Valid @RequestBody CompanySettingsUpdateDto request) {
        return ApiResponse.success(companySettingsService.updateSettings(request));
    }
}
