package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.PaymentVoucherDisplayDto;
import com.erp.system.accounting.dto.form.PaymentVoucherFormDto;
import com.erp.system.accounting.service.PaymentVoucherService;
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
@RequestMapping("/accounting/payment-vouchers")
@RequiredArgsConstructor
public class PaymentVoucherController {

    private final PaymentVoucherService paymentVoucherService;

    @GetMapping
    public ApiResponse<List<PaymentVoucherDisplayDto>> getPaymentVouchers(
            @RequestParam(required = false) VoucherStatus status,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) Long bankAccountId,
            @RequestParam(required = false) String beneficiary,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(paymentVoucherService.getPaymentVouchers(
                status, paymentMethod, bankAccountId, beneficiary, minAmount, maxAmount, fromDate, toDate, search
        ));
    }

    @GetMapping("/{voucherId}")
    public ApiResponse<PaymentVoucherDisplayDto> getPaymentVoucher(@PathVariable Long voucherId) {
        return ApiResponse.success(paymentVoucherService.getPaymentVoucher(voucherId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentVoucherDisplayDto> createPaymentVoucher(@Valid @RequestBody PaymentVoucherFormDto request) {
        return ApiResponse.success(paymentVoucherService.createPaymentVoucher(request));
    }

    @PutMapping("/{voucherId}")
    public ApiResponse<PaymentVoucherDisplayDto> updatePaymentVoucher(@PathVariable Long voucherId,
                                                                      @Valid @RequestBody PaymentVoucherFormDto request) {
        return ApiResponse.success(paymentVoucherService.updatePaymentVoucher(voucherId, request));
    }

    @PostMapping("/{voucherId}/approve")
    public ApiResponse<PaymentVoucherDisplayDto> approvePaymentVoucher(@PathVariable Long voucherId,
                                                                       @RequestParam String actor) {
        return ApiResponse.success(paymentVoucherService.approvePaymentVoucher(voucherId, actor));
    }

    @PostMapping("/{voucherId}/post")
    public ApiResponse<PaymentVoucherDisplayDto> postPaymentVoucher(@PathVariable Long voucherId,
                                                                    @RequestParam String actor) {
        return ApiResponse.success(paymentVoucherService.postPaymentVoucher(voucherId, actor));
    }

    @PostMapping("/{voucherId}/cancel")
    public ApiResponse<PaymentVoucherDisplayDto> cancelPaymentVoucher(@PathVariable Long voucherId,
                                                                      @RequestParam String actor,
                                                                      @RequestParam(required = false) String reason) {
        return ApiResponse.success(paymentVoucherService.cancelPaymentVoucher(voucherId, actor, reason));
    }
}
