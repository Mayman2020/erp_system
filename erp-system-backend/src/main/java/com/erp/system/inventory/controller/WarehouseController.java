package com.erp.system.inventory.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.dto.PageResponse;
import com.erp.system.inventory.dto.display.WarehouseDisplayDto;
import com.erp.system.inventory.dto.form.WarehouseFormDto;
import com.erp.system.inventory.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public ApiResponse<List<WarehouseDisplayDto>> getWarehouses(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(warehouseService.getWarehouses(active, search));
    }

    @GetMapping("/paged")
    public ApiResponse<PageResponse<WarehouseDisplayDto>> getWarehousesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by("code").ascending());
        return ApiResponse.success(warehouseService.getWarehousesPaged(active, q, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseDisplayDto> getWarehouse(@PathVariable Long id) {
        return ApiResponse.success(warehouseService.getWarehouse(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WarehouseDisplayDto> createWarehouse(@Valid @RequestBody WarehouseFormDto request) {
        return ApiResponse.success(warehouseService.createWarehouse(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseDisplayDto> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseFormDto request
    ) {
        return ApiResponse.success(warehouseService.updateWarehouse(id, request));
    }

    @PutMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivateWarehouse(@PathVariable Long id) {
        warehouseService.deactivateWarehouse(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> activateWarehouse(@PathVariable Long id) {
        warehouseService.activateWarehouse(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ApiResponse.success(null);
    }
}
