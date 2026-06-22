package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.purchases.dto.display.PurchaseReturnLineDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseReturnLineFormDto;
import com.erp.system.purchases.service.PurchaseReturnLineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases/return-lines")
@RequiredArgsConstructor
public class PurchaseReturnLineController {

    private final PurchaseReturnLineService purchaseReturnLineService;

    @GetMapping
    public ApiResponse<List<PurchaseReturnLineDisplayDto>> getAll() {
        return ApiResponse.success(purchaseReturnLineService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseReturnLineDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(purchaseReturnLineService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseReturnLineDisplayDto> create(@Valid @RequestBody PurchaseReturnLineFormDto request) {
        return ApiResponse.success(purchaseReturnLineService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseReturnLineDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseReturnLineFormDto request) {
        return ApiResponse.success(purchaseReturnLineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        purchaseReturnLineService.delete(id);
        return ApiResponse.success(null);
    }

}
