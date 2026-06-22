package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.purchases.dto.display.SupplierDisplayDto;
import com.erp.system.purchases.dto.form.SupplierFormDto;
import com.erp.system.purchases.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ApiResponse<List<SupplierDisplayDto>> getAll() {
        return ApiResponse.success(supplierService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<SupplierDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(supplierService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SupplierDisplayDto> create(@Valid @RequestBody SupplierFormDto request) {
        return ApiResponse.success(supplierService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SupplierDisplayDto> update(@PathVariable Long id, @Valid @RequestBody SupplierFormDto request) {
        return ApiResponse.success(supplierService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ApiResponse.success(null);
    }

}
