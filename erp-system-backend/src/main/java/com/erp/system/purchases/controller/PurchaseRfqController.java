package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.purchases.dto.display.PurchaseRfqDisplayDto;
import com.erp.system.purchases.dto.display.PurchaseRfqQuoteDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseRfqFormDto;
import com.erp.system.purchases.dto.form.PurchaseRfqQuoteInputDto;
import com.erp.system.purchases.service.PurchaseRfqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases/rfqs")
@RequiredArgsConstructor
public class PurchaseRfqController {

    private final PurchaseRfqService rfqService;

    @GetMapping
    public ApiResponse<List<PurchaseRfqDisplayDto>> getAll() {
        return ApiResponse.success(rfqService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseRfqDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(rfqService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseRfqDisplayDto> create(@Valid @RequestBody PurchaseRfqFormDto request) {
        return ApiResponse.success(rfqService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseRfqDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseRfqFormDto request) {
        return ApiResponse.success(rfqService.update(id, request));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<PurchaseRfqDisplayDto> submit(@PathVariable Long id) {
        return ApiResponse.success(rfqService.submit(id));
    }

    @PostMapping("/{id}/quotes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseRfqQuoteDisplayDto> addQuote(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseRfqQuoteInputDto request
    ) {
        return ApiResponse.success(rfqService.addQuote(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        rfqService.delete(id);
        return ApiResponse.success(null);
    }
}
