package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.dto.PageResponse;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.purchases.dto.display.PurchaseInvoiceDisplayDto;
import com.erp.system.purchases.dto.display.PurchaseOrderDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseOrderFormDto;
import com.erp.system.purchases.service.PurchaseInvoiceService;
import com.erp.system.purchases.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

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

    @GetMapping("/paged")
    public ApiResponse<PageResponse<PurchaseOrderDisplayDto>> getPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Order.desc("orderDate"), Sort.Order.desc("id")));
        return ApiResponse.success(purchaseOrderService.getPaged(status, q, fromDate, toDate, pageable));
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
