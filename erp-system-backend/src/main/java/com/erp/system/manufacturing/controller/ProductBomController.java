package com.erp.system.manufacturing.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.manufacturing.dto.display.ProductBomLineDisplayDto;
import com.erp.system.manufacturing.dto.form.ProductBomLineFormDto;
import com.erp.system.manufacturing.service.ProductBomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manufacturing/bom")
@RequiredArgsConstructor
public class ProductBomController {

    private final ProductBomService productBomService;

    @GetMapping
    public ApiResponse<List<ProductBomLineDisplayDto>> getByParentProductId(@RequestParam Long parentProductId) {
        return ApiResponse.success(productBomService.getByParentProductId(parentProductId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductBomLineDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(productBomService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductBomLineDisplayDto> create(@Valid @RequestBody ProductBomLineFormDto request) {
        return ApiResponse.success(productBomService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductBomLineDisplayDto> update(@PathVariable Long id,
                                                        @Valid @RequestBody ProductBomLineFormDto request) {
        return ApiResponse.success(productBomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productBomService.delete(id);
        return ApiResponse.success(null);
    }
}
