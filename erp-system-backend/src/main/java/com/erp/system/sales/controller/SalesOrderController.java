package com.erp.system.sales.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.sales.dto.display.SalesOrderDisplayDto;
import com.erp.system.sales.dto.form.SalesOrderFormDto;
import com.erp.system.sales.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sales/orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService orderService;

    @GetMapping
    public ApiResponse<List<SalesOrderDisplayDto>> getOrders(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ApiResponse.success(orderService.getOrders(status, search, fromDate, toDate));
    }

    @GetMapping("/{id}")
    public ApiResponse<SalesOrderDisplayDto> getOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrder(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SalesOrderDisplayDto> createOrder(@Valid @RequestBody SalesOrderFormDto request) {
        return ApiResponse.success(orderService.createOrder(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SalesOrderDisplayDto> updateOrder(@PathVariable Long id,
                                                         @Valid @RequestBody SalesOrderFormDto request) {
        return ApiResponse.success(orderService.updateOrder(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<SalesOrderDisplayDto> approveOrder(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(orderService.approveOrder(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SalesOrderDisplayDto> cancelOrder(@PathVariable Long id,
                                                         @RequestParam String actor,
                                                         @RequestParam(required = false) String reason) {
        return ApiResponse.success(orderService.cancelOrder(id, actor, reason));
    }
}
