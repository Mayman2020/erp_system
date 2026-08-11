package com.erp.system.admin.controller;

import com.erp.system.admin.dto.display.LicenseDisplayDto;
import com.erp.system.admin.dto.form.LicenseActivateFormDto;
import com.erp.system.admin.service.LicenseService;
import com.erp.system.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/license")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;

    @GetMapping
    public ApiResponse<LicenseDisplayDto> getCurrent() {
        return ApiResponse.success(licenseService.getCurrent());
    }

    @PostMapping("/activate")
    public ApiResponse<LicenseDisplayDto> activate(@Valid @RequestBody LicenseActivateFormDto request) {
        return ApiResponse.success(licenseService.activate(request));
    }
}
