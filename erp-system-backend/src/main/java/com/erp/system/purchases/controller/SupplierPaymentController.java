package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.purchases.dto.display.SupplierPaymentDisplayDto;
import com.erp.system.purchases.dto.form.SupplierPaymentFormDto;
import com.erp.system.purchases.service.SupplierPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases/payments")
@RequiredArgsConstructor
public class SupplierPaymentController {

    private final SupplierPaymentService supplierPaymentService;

    @GetMapping
    public ApiResponse<List<SupplierPaymentDisplayDto>> getAll() {
        return ApiResponse.success(supplierPaymentService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<SupplierPaymentDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(supplierPaymentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SupplierPaymentDisplayDto> create(@Valid @RequestBody SupplierPaymentFormDto request) {
        return ApiResponse.success(supplierPaymentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SupplierPaymentDisplayDto> update(@PathVariable Long id, @Valid @RequestBody SupplierPaymentFormDto request) {
        return ApiResponse.success(supplierPaymentService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<SupplierPaymentDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(supplierPaymentService.approve(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SupplierPaymentDisplayDto> cancel(@PathVariable Long id,
                                                         @RequestParam String actor,
                                                         @RequestParam(required = false) String reason) {
        return ApiResponse.success(supplierPaymentService.cancel(id, actor, reason));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        supplierPaymentService.delete(id);
        return ApiResponse.success(null);
    }
}
