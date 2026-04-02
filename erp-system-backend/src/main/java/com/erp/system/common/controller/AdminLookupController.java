package com.erp.system.common.controller;

import com.erp.system.common.dto.AdminLookupTypeDto;
import com.erp.system.common.dto.AdminLookupTypeFormDto;
import com.erp.system.common.dto.AdminLookupValueDto;
import com.erp.system.common.dto.AdminLookupValueFormDto;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.service.AdminLookupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/lookups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLookupController {

    private final AdminLookupService adminLookupService;

    @GetMapping("/types")
    public ApiResponse<List<AdminLookupTypeDto>> getLookupTypes() {
        return ApiResponse.success(adminLookupService.getLookupTypes());
    }

    @PostMapping("/types")
    public ApiResponse<AdminLookupTypeDto> createLookupType(@Valid @RequestBody AdminLookupTypeFormDto request) {
        return ApiResponse.success(adminLookupService.createLookupType(request));
    }

    @PutMapping("/types/{typeId}")
    public ApiResponse<AdminLookupTypeDto> updateLookupType(@PathVariable Long typeId, @Valid @RequestBody AdminLookupTypeFormDto request) {
        return ApiResponse.success(adminLookupService.updateLookupType(typeId, request));
    }

    @DeleteMapping("/types/{typeId}")
    public ApiResponse<Boolean> deleteLookupType(@PathVariable Long typeId) {
        adminLookupService.deleteLookupType(typeId);
        return ApiResponse.success(true);
    }

    @GetMapping("/values")
    public ApiResponse<List<AdminLookupValueDto>> getLookupValues(@RequestParam String typeCode) {
        return ApiResponse.success(adminLookupService.getLookupValues(typeCode));
    }

    @PostMapping("/values")
    public ApiResponse<AdminLookupValueDto> createLookupValue(@Valid @RequestBody AdminLookupValueFormDto request) {
        return ApiResponse.success(adminLookupService.createLookupValue(request));
    }

    @PutMapping("/values/{valueId}")
    public ApiResponse<AdminLookupValueDto> updateLookupValue(@PathVariable Long valueId, @Valid @RequestBody AdminLookupValueFormDto request) {
        return ApiResponse.success(adminLookupService.updateLookupValue(valueId, request));
    }

    @DeleteMapping("/values/{valueId}")
    public ApiResponse<Boolean> deleteLookupValue(@PathVariable Long valueId) {
        adminLookupService.deleteLookupValue(valueId);
        return ApiResponse.success(true);
    }
}
