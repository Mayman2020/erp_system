package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.purchases.dto.display.PurchaseOrderLineDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseOrderLineFormDto;
import com.erp.system.purchases.service.PurchaseOrderLineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases/order-lines")
@RequiredArgsConstructor
public class PurchaseOrderLineController {

    private final PurchaseOrderLineService purchaseOrderLineService;

    @GetMapping
    public ApiResponse<List<PurchaseOrderLineDisplayDto>> getAll() {
        return ApiResponse.success(purchaseOrderLineService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrderLineDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(purchaseOrderLineService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseOrderLineDisplayDto> create(@Valid @RequestBody PurchaseOrderLineFormDto request) {
        return ApiResponse.success(purchaseOrderLineService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseOrderLineDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderLineFormDto request) {
        return ApiResponse.success(purchaseOrderLineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        purchaseOrderLineService.delete(id);
        return ApiResponse.success(null);
    }

}
