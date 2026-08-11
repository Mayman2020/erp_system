package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.dto.PageResponse;
import com.erp.system.purchases.dto.display.SupplierDisplayDto;
import com.erp.system.purchases.dto.form.SupplierFormDto;
import com.erp.system.purchases.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @GetMapping("/paged")
    public ApiResponse<PageResponse<SupplierDisplayDto>> getPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by("id").descending());
        return ApiResponse.success(supplierService.getPaged(active, q, pageable));
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
