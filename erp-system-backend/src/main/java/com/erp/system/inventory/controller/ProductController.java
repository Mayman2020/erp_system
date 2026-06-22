package com.erp.system.inventory.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.inventory.dto.display.ProductDisplayDto;
import com.erp.system.inventory.dto.form.ProductFormDto;
import com.erp.system.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<List<ProductDisplayDto>> getProducts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(productService.getProducts(active, categoryId, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDisplayDto> getProduct(@PathVariable Long id) {
        return ApiResponse.success(productService.getProduct(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductDisplayDto> createProduct(@Valid @RequestBody ProductFormDto request) {
        return ApiResponse.success(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDisplayDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductFormDto request
    ) {
        return ApiResponse.success(productService.updateProduct(id, request));
    }

    @PutMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> activateProduct(@PathVariable Long id) {
        productService.activateProduct(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.success(null);
    }
}
