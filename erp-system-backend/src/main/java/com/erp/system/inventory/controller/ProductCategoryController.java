package com.erp.system.inventory.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.inventory.dto.display.ProductCategoryDisplayDto;
import com.erp.system.inventory.dto.form.ProductCategoryFormDto;
import com.erp.system.inventory.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @GetMapping
    public ApiResponse<List<ProductCategoryDisplayDto>> getCategories(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(categoryService.getCategories(active, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductCategoryDisplayDto> getCategory(@PathVariable Long id) {
        return ApiResponse.success(categoryService.getCategory(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductCategoryDisplayDto> createCategory(@Valid @RequestBody ProductCategoryFormDto request) {
        return ApiResponse.success(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductCategoryDisplayDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ProductCategoryFormDto request
    ) {
        return ApiResponse.success(categoryService.updateCategory(id, request));
    }

    @PutMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deactivateCategory(@PathVariable Long id) {
        categoryService.deactivateCategory(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> activateCategory(@PathVariable Long id) {
        categoryService.activateCategory(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success(null);
    }
}
