package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.ExchangeRateDisplayDto;
import com.erp.system.accounting.dto.form.ExchangeRateFormDto;
import com.erp.system.accounting.service.CurrencyConversionService;
import com.erp.system.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final CurrencyConversionService currencyConversionService;

    @GetMapping
    public ApiResponse<List<ExchangeRateDisplayDto>> getRates() {
        return ApiResponse.success(currencyConversionService.getAllRates());
    }

    @GetMapping("/{id}")
    public ApiResponse<ExchangeRateDisplayDto> getRate(@PathVariable Long id) {
        return ApiResponse.success(currencyConversionService.getRateById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ExchangeRateDisplayDto> createRate(@Valid @RequestBody ExchangeRateFormDto form) {
        return ApiResponse.success(currencyConversionService.createRate(form));
    }

    @PutMapping("/{id}")
    public ApiResponse<ExchangeRateDisplayDto> updateRate(@PathVariable Long id,
                                                          @Valid @RequestBody ExchangeRateFormDto form) {
        return ApiResponse.success(currencyConversionService.updateRate(id, form));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRate(@PathVariable Long id) {
        currencyConversionService.deleteRate(id);
        return ApiResponse.success(null);
    }
}
