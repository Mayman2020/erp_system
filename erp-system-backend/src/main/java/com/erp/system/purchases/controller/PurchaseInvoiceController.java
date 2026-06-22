package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.purchases.dto.display.PurchaseInvoiceDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseInvoiceFormDto;
import com.erp.system.purchases.service.PurchaseInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases/invoices")
@RequiredArgsConstructor
public class PurchaseInvoiceController {

    private final PurchaseInvoiceService purchaseInvoiceService;

    @GetMapping
    public ApiResponse<List<PurchaseInvoiceDisplayDto>> getAll() {
        return ApiResponse.success(purchaseInvoiceService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseInvoiceDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(purchaseInvoiceService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseInvoiceDisplayDto> create(@Valid @RequestBody PurchaseInvoiceFormDto request) {
        return ApiResponse.success(purchaseInvoiceService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseInvoiceDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseInvoiceFormDto request) {
        return ApiResponse.success(purchaseInvoiceService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<PurchaseInvoiceDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(purchaseInvoiceService.approve(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<PurchaseInvoiceDisplayDto> cancel(@PathVariable Long id,
                                                         @RequestParam String actor,
                                                         @RequestParam(required = false) String reason) {
        return ApiResponse.success(purchaseInvoiceService.cancel(id, actor, reason));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        purchaseInvoiceService.delete(id);
        return ApiResponse.success(null);
    }
}
