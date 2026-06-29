package com.erp.system.sales.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.sales.dto.display.SalesOrderDisplayDto;
import com.erp.system.sales.dto.display.SalesQuotationDisplayDto;
import com.erp.system.sales.dto.form.SalesQuotationFormDto;
import com.erp.system.sales.service.SalesQuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sales/quotations")
@RequiredArgsConstructor
public class SalesQuotationController {

    private final SalesQuotationService quotationService;

    @GetMapping
    public ApiResponse<List<SalesQuotationDisplayDto>> getQuotations(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.success(quotationService.getQuotations(status, search, fromDate, toDate));
    }

    @GetMapping("/{id}")
    public ApiResponse<SalesQuotationDisplayDto> getQuotation(@PathVariable Long id) {
        return ApiResponse.success(quotationService.getQuotation(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SalesQuotationDisplayDto> createQuotation(@Valid @RequestBody SalesQuotationFormDto request) {
        return ApiResponse.success(quotationService.createQuotation(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SalesQuotationDisplayDto> updateQuotation(@PathVariable Long id,
                                                                   @Valid @RequestBody SalesQuotationFormDto request) {
        return ApiResponse.success(quotationService.updateQuotation(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteQuotation(@PathVariable Long id) {
        quotationService.deleteQuotation(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<SalesQuotationDisplayDto> approveQuotation(@PathVariable Long id,
                                                                  @RequestParam String actor) {
        return ApiResponse.success(quotationService.approveQuotation(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SalesQuotationDisplayDto> cancelQuotation(@PathVariable Long id,
                                                                 @RequestParam String actor,
                                                                 @RequestParam(required = false) String reason) {
        return ApiResponse.success(quotationService.cancelQuotation(id, actor, reason));
    }

    @PostMapping("/{id}/convert-to-order")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SalesOrderDisplayDto> convertToOrder(@PathVariable Long id) {
        return ApiResponse.success(quotationService.convertToOrder(id));
    }
}
