package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.purchases.dto.display.PurchaseInvoiceDisplayDto;
import com.erp.system.purchases.dto.display.PurchaseOrderDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseOrderFormDto;
import com.erp.system.purchases.service.PurchaseInvoiceService;
import com.erp.system.purchases.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseInvoiceService purchaseInvoiceService;

    @GetMapping
    public ApiResponse<List<PurchaseOrderDisplayDto>> getAll() {
        return ApiResponse.success(purchaseOrderService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrderDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(purchaseOrderService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseOrderDisplayDto> create(@Valid @RequestBody PurchaseOrderFormDto request) {
        return ApiResponse.success(purchaseOrderService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseOrderDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderFormDto request) {
        return ApiResponse.success(purchaseOrderService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<PurchaseOrderDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(purchaseOrderService.approve(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<PurchaseOrderDisplayDto> cancel(@PathVariable Long id,
                                                       @RequestParam String actor,
                                                       @RequestParam(required = false) String reason) {
        return ApiResponse.success(purchaseOrderService.cancel(id, actor, reason));
    }

    @PostMapping("/{id}/convert-to-invoice")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseInvoiceDisplayDto> convertToInvoice(@PathVariable Long id) {
        return ApiResponse.success(purchaseInvoiceService.create(purchaseOrderService.buildInvoiceForm(id)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        purchaseOrderService.delete(id);
        return ApiResponse.success(null);
    }
}
