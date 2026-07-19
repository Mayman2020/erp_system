package com.erp.system.ui.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.ui.dto.ScreenSettingDto;
import com.erp.system.ui.dto.ScreenSettingFormDto;
import com.erp.system.ui.service.ScreenSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/screen-settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminScreenSettingsController {

    private final ScreenSettingsService screenSettingsService;

    @GetMapping
    public ApiResponse<List<ScreenSettingDto>> list() {
        return ApiResponse.success(screenSettingsService.list());
    }

    @PutMapping("/{screenKey}")
    public ApiResponse<ScreenSettingDto> upsert(@PathVariable String screenKey, @Valid @RequestBody ScreenSettingFormDto request) {
        return ApiResponse.success(screenSettingsService.upsert(screenKey, request));
    }
}
