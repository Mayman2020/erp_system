package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.CustomerInvoiceDisplayDto;
import com.erp.system.accounting.dto.form.CustomerInvoiceFormDto;
import com.erp.system.accounting.service.CustomerInvoiceService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.InvoiceStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/invoices")
@RequiredArgsConstructor
public class CustomerInvoiceController {

    private final CustomerInvoiceService invoiceService;

    @GetMapping
    public ApiResponse<List<CustomerInvoiceDisplayDto>> getInvoices(@RequestParam(required = false) InvoiceStatus status,
                                                                     @RequestParam(required = false) String search) {
        return ApiResponse.success(invoiceService.getInvoices(status, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerInvoiceDisplayDto> getInvoice(@PathVariable Long id) {
        return ApiResponse.success(invoiceService.getInvoice(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerInvoiceDisplayDto> createInvoice(@Valid @RequestBody CustomerInvoiceFormDto request) {
        return ApiResponse.success(invoiceService.createInvoice(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerInvoiceDisplayDto> updateInvoice(@PathVariable Long id, @Valid @RequestBody CustomerInvoiceFormDto request) {
        return ApiResponse.success(invoiceService.updateInvoice(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<CustomerInvoiceDisplayDto> approveInvoice(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(invoiceService.postInvoice(id, actor));
    }

    @PostMapping("/{id}/post")
    public ApiResponse<CustomerInvoiceDisplayDto> postInvoice(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(invoiceService.postInvoice(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<CustomerInvoiceDisplayDto> cancelInvoice(@PathVariable Long id,
                                                                 @RequestParam String actor,
                                                                 @RequestParam(required = false) String reason) {
        return ApiResponse.success(invoiceService.cancelInvoice(id, actor, reason));
    }
}