package com.erp.system.partners.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.partners.dto.display.ProfitDistributionDisplayDto;
import com.erp.system.partners.dto.form.ProfitDistributionFormDto;
import com.erp.system.partners.service.ProfitDistributionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/partners/distributions")
@RequiredArgsConstructor
public class ProfitDistributionController {

    private final ProfitDistributionService profitDistributionService;

    @GetMapping
    public ApiResponse<List<ProfitDistributionDisplayDto>> getAll() {
        return ApiResponse.success(profitDistributionService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProfitDistributionDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(profitDistributionService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProfitDistributionDisplayDto> create(@Valid @RequestBody ProfitDistributionFormDto request) {
        return ApiResponse.success(profitDistributionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProfitDistributionDisplayDto> update(@PathVariable Long id,
                                                            @Valid @RequestBody ProfitDistributionFormDto request) {
        return ApiResponse.success(profitDistributionService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<ProfitDistributionDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(profitDistributionService.approve(id, actor));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        profitDistributionService.delete(id);
        return ApiResponse.success(null);
    }
}
