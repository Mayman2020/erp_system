package com.erp.system.inventory.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.inventory.dto.display.ProductUomConversionDisplayDto;
import com.erp.system.inventory.dto.form.ProductUomConversionFormDto;
import com.erp.system.inventory.service.ProductUomConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/products/{productId}/uoms")
@RequiredArgsConstructor
public class ProductUomConversionController {

    private final ProductUomConversionService conversionService;

    @GetMapping
    public ApiResponse<List<ProductUomConversionDisplayDto>> list(@PathVariable Long productId) {
        return ApiResponse.success(conversionService.listByProduct(productId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductUomConversionDisplayDto> get(@PathVariable Long productId, @PathVariable Long id) {
        return ApiResponse.success(conversionService.getById(productId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductUomConversionDisplayDto> create(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUomConversionFormDto request
    ) {
        return ApiResponse.success(conversionService.create(productId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductUomConversionDisplayDto> update(
            @PathVariable Long productId,
            @PathVariable Long id,
            @Valid @RequestBody ProductUomConversionFormDto request
    ) {
        return ApiResponse.success(conversionService.update(productId, id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long productId, @PathVariable Long id) {
        conversionService.delete(productId, id);
        return ApiResponse.success(null);
    }
}
