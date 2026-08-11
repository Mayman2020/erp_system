package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.purchases.dto.display.GoodsReceiptDisplayDto;
import com.erp.system.purchases.dto.form.GoodsReceiptFormDto;
import com.erp.system.purchases.service.GoodsReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases/receipts")
@RequiredArgsConstructor
public class GoodsReceiptController {

    private final GoodsReceiptService receiptService;

    @GetMapping
    public ApiResponse<List<GoodsReceiptDisplayDto>> getAll() {
        return ApiResponse.success(receiptService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<GoodsReceiptDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(receiptService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GoodsReceiptDisplayDto> create(@Valid @RequestBody GoodsReceiptFormDto request) {
        return ApiResponse.success(receiptService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<GoodsReceiptDisplayDto> update(@PathVariable Long id, @Valid @RequestBody GoodsReceiptFormDto request) {
        return ApiResponse.success(receiptService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<GoodsReceiptDisplayDto> approve(@PathVariable Long id) {
        return ApiResponse.success(receiptService.approve(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        receiptService.delete(id);
        return ApiResponse.success(null);
    }
}
