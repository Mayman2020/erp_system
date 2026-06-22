package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.purchases.dto.display.PurchaseInvoiceLineDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseInvoiceLineFormDto;
import com.erp.system.purchases.service.PurchaseInvoiceLineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases/invoice-lines")
@RequiredArgsConstructor
public class PurchaseInvoiceLineController {

    private final PurchaseInvoiceLineService purchaseInvoiceLineService;

    @GetMapping
    public ApiResponse<List<PurchaseInvoiceLineDisplayDto>> getAll() {
        return ApiResponse.success(purchaseInvoiceLineService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseInvoiceLineDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(purchaseInvoiceLineService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseInvoiceLineDisplayDto> create(@Valid @RequestBody PurchaseInvoiceLineFormDto request) {
        return ApiResponse.success(purchaseInvoiceLineService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseInvoiceLineDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseInvoiceLineFormDto request) {
        return ApiResponse.success(purchaseInvoiceLineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        purchaseInvoiceLineService.delete(id);
        return ApiResponse.success(null);
    }

}
