package com.erp.system.maintenance.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.maintenance.dto.display.MaintenanceAssetDisplayDto;
import com.erp.system.maintenance.dto.form.MaintenanceAssetFormDto;
import com.erp.system.maintenance.service.MaintenanceAssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenance/assets")
@RequiredArgsConstructor
public class MaintenanceAssetController {

    private final MaintenanceAssetService assetService;

    @GetMapping
    public ApiResponse<List<MaintenanceAssetDisplayDto>> getAll(@RequestParam(required = false) String status) {
        return ApiResponse.success(assetService.getAll(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<MaintenanceAssetDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(assetService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MaintenanceAssetDisplayDto> create(@Valid @RequestBody MaintenanceAssetFormDto request) {
        return ApiResponse.success(assetService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MaintenanceAssetDisplayDto> update(@PathVariable Long id, @Valid @RequestBody MaintenanceAssetFormDto request) {
        return ApiResponse.success(assetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        assetService.delete(id);
        return ApiResponse.success(null);
    }
}
