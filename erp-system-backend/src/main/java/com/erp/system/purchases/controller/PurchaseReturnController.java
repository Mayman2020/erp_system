package com.erp.system.purchases.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.purchases.dto.display.PurchaseReturnDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseReturnFormDto;
import com.erp.system.purchases.service.PurchaseReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases/returns")
@RequiredArgsConstructor
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    @GetMapping
    public ApiResponse<List<PurchaseReturnDisplayDto>> getAll() {
        return ApiResponse.success(purchaseReturnService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseReturnDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(purchaseReturnService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseReturnDisplayDto> create(@Valid @RequestBody PurchaseReturnFormDto request) {
        return ApiResponse.success(purchaseReturnService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseReturnDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseReturnFormDto request) {
        return ApiResponse.success(purchaseReturnService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<PurchaseReturnDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(purchaseReturnService.approve(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<PurchaseReturnDisplayDto> cancel(@PathVariable Long id,
                                                        @RequestParam String actor,
                                                        @RequestParam(required = false) String reason) {
        return ApiResponse.success(purchaseReturnService.cancel(id, actor, reason));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        purchaseReturnService.delete(id);
        return ApiResponse.success(null);
    }
}
