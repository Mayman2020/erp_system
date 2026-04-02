package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.ReceiptVoucherDisplayDto;
import com.erp.system.accounting.dto.form.ReceiptVoucherFormDto;
import com.erp.system.accounting.service.ReceiptVoucherService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.PaymentMethod;
import com.erp.system.common.enums.VoucherStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accounting/receipt-vouchers")
@RequiredArgsConstructor
public class ReceiptVoucherController {

    private final ReceiptVoucherService receiptVoucherService;

    @GetMapping
    public ApiResponse<List<ReceiptVoucherDisplayDto>> getReceiptVouchers(
            @RequestParam(required = false) VoucherStatus status,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) Long bankAccountId,
            @RequestParam(required = false) String payer,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(receiptVoucherService.getReceiptVouchers(
                status, paymentMethod, bankAccountId, payer, minAmount, maxAmount, fromDate, toDate, search
        ));
    }

    @GetMapping("/{voucherId}")
    public ApiResponse<ReceiptVoucherDisplayDto> getReceiptVoucher(@PathVariable Long voucherId) {
        return ApiResponse.success(receiptVoucherService.getReceiptVoucher(voucherId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReceiptVoucherDisplayDto> createReceiptVoucher(@Valid @RequestBody ReceiptVoucherFormDto request) {
        return ApiResponse.success(receiptVoucherService.createReceiptVoucher(request));
    }

    @PutMapping("/{voucherId}")
    public ApiResponse<ReceiptVoucherDisplayDto> updateReceiptVoucher(@PathVariable Long voucherId,
                                                                      @Valid @RequestBody ReceiptVoucherFormDto request) {
        return ApiResponse.success(receiptVoucherService.updateReceiptVoucher(voucherId, request));
    }

    @PostMapping("/{voucherId}/approve")
    public ApiResponse<ReceiptVoucherDisplayDto> approveReceiptVoucher(@PathVariable Long voucherId,
                                                                       @RequestParam String actor) {
        return ApiResponse.success(receiptVoucherService.approveReceiptVoucher(voucherId, actor));
    }

    @PostMapping("/{voucherId}/post")
    public ApiResponse<ReceiptVoucherDisplayDto> postReceiptVoucher(@PathVariable Long voucherId,
                                                                    @RequestParam String actor) {
        return ApiResponse.success(receiptVoucherService.postReceiptVoucher(voucherId, actor));
    }

    @PostMapping("/{voucherId}/cancel")
    public ApiResponse<ReceiptVoucherDisplayDto> cancelReceiptVoucher(@PathVariable Long voucherId,
                                                                      @RequestParam String actor,
                                                                      @RequestParam(required = false) String reason) {
        return ApiResponse.success(receiptVoucherService.cancelReceiptVoucher(voucherId, actor, reason));
    }
}
