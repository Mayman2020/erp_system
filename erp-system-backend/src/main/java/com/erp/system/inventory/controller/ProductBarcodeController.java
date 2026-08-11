package com.erp.system.inventory.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.inventory.dto.display.ProductBarcodeDisplayDto;
import com.erp.system.inventory.dto.form.ProductBarcodeFormDto;
import com.erp.system.inventory.service.ProductBarcodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/products/{productId}/barcodes")
@RequiredArgsConstructor
public class ProductBarcodeController {

    private final ProductBarcodeService barcodeService;

    @GetMapping
    public ApiResponse<List<ProductBarcodeDisplayDto>> list(@PathVariable Long productId) {
        return ApiResponse.success(barcodeService.listByProduct(productId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductBarcodeDisplayDto> get(@PathVariable Long productId, @PathVariable Long id) {
        return ApiResponse.success(barcodeService.getById(productId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductBarcodeDisplayDto> create(
            @PathVariable Long productId,
            @Valid @RequestBody ProductBarcodeFormDto request
    ) {
        return ApiResponse.success(barcodeService.create(productId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductBarcodeDisplayDto> update(
            @PathVariable Long productId,
            @PathVariable Long id,
            @Valid @RequestBody ProductBarcodeFormDto request
    ) {
        return ApiResponse.success(barcodeService.update(productId, id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long productId, @PathVariable Long id) {
        barcodeService.delete(productId, id);
        return ApiResponse.success(null);
    }
}
