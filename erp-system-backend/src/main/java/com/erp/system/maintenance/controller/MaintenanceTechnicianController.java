package com.erp.system.maintenance.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.maintenance.dto.display.MaintenanceTechnicianDisplayDto;
import com.erp.system.maintenance.dto.form.MaintenanceTechnicianFormDto;
import com.erp.system.maintenance.service.MaintenanceTechnicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenance/technicians")
@RequiredArgsConstructor
public class MaintenanceTechnicianController {

    private final MaintenanceTechnicianService technicianService;

    @GetMapping
    public ApiResponse<List<MaintenanceTechnicianDisplayDto>> getAll(@RequestParam(required = false) Boolean activeOnly) {
        return ApiResponse.success(technicianService.getAll(activeOnly));
    }

    @GetMapping("/{id}")
    public ApiResponse<MaintenanceTechnicianDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(technicianService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MaintenanceTechnicianDisplayDto> create(@Valid @RequestBody MaintenanceTechnicianFormDto request) {
        return ApiResponse.success(technicianService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MaintenanceTechnicianDisplayDto> update(@PathVariable Long id, @Valid @RequestBody MaintenanceTechnicianFormDto request) {
        return ApiResponse.success(technicianService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        technicianService.delete(id);
        return ApiResponse.success(null);
    }
}
