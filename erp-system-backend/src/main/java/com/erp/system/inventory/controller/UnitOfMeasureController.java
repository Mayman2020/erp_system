package com.erp.system.inventory.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.inventory.dto.display.UnitOfMeasureDisplayDto;
import com.erp.system.inventory.dto.form.UnitOfMeasureFormDto;
import com.erp.system.inventory.service.UnitOfMeasureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/units")
@RequiredArgsConstructor
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitService;

    @GetMapping
    public ApiResponse<List<UnitOfMeasureDisplayDto>> getUnits(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(unitService.getUnits(active, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<UnitOfMeasureDisplayDto> getUnit(@PathVariable Long id) {
        return ApiResponse.success(unitService.getUnit(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UnitOfMeasureDisplayDto> createUnit(@Valid @RequestBody UnitOfMeasureFormDto request) {
        return ApiResponse.success(unitService.createUnit(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UnitOfMeasureDisplayDto> updateUnit(
            @PathVariable Long id,
            @Valid @RequestBody UnitOfMeasureFormDto request
    ) {
        return ApiResponse.success(unitService.updateUnit(id, request));
    }

    @PutMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivateUnit(@PathVariable Long id) {
        unitService.deactivateUnit(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> activateUnit(@PathVariable Long id) {
        unitService.activateUnit(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteUnit(@PathVariable Long id) {
        unitService.deleteUnit(id);
        return ApiResponse.success(null);
    }
}
