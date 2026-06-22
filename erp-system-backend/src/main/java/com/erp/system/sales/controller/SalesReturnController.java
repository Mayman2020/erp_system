package com.erp.system.sales.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.sales.dto.display.SalesReturnDisplayDto;
import com.erp.system.sales.dto.form.SalesReturnFormDto;
import com.erp.system.sales.service.SalesReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sales/returns")
@RequiredArgsConstructor
public class SalesReturnController {

    private final SalesReturnService returnService;

    @GetMapping
    public ApiResponse<List<SalesReturnDisplayDto>> getReturns(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.success(returnService.getReturns(status, search, fromDate, toDate));
    }

    @GetMapping("/{id}")
    public ApiResponse<SalesReturnDisplayDto> getReturn(@PathVariable Long id) {
        return ApiResponse.success(returnService.getReturn(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SalesReturnDisplayDto> createReturn(@Valid @RequestBody SalesReturnFormDto request) {
        return ApiResponse.success(returnService.createReturn(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SalesReturnDisplayDto> updateReturn(@PathVariable Long id,
                                                           @Valid @RequestBody SalesReturnFormDto request) {
        return ApiResponse.success(returnService.updateReturn(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteReturn(@PathVariable Long id) {
        returnService.deleteReturn(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<SalesReturnDisplayDto> approveReturn(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(returnService.approveReturn(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SalesReturnDisplayDto> cancelReturn(@PathVariable Long id,
                                                           @RequestParam String actor,
                                                           @RequestParam(required = false) String reason) {
        return ApiResponse.success(returnService.cancelReturn(id, actor, reason));
    }
}
