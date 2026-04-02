package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.BudgetDisplayDto;
import com.erp.system.accounting.dto.form.BudgetFormDto;
import com.erp.system.accounting.service.BudgetService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.BudgetStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ApiResponse<List<BudgetDisplayDto>> getBudgets(@RequestParam(required = false) BudgetStatus status) {
        return ApiResponse.success(budgetService.getBudgets(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<BudgetDisplayDto> getBudget(@PathVariable Long id) {
        return ApiResponse.success(budgetService.getBudget(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BudgetDisplayDto> createBudget(@Valid @RequestBody BudgetFormDto request) {
        return ApiResponse.success(budgetService.createBudget(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BudgetDisplayDto> updateBudget(@PathVariable Long id, @Valid @RequestBody BudgetFormDto request) {
        return ApiResponse.success(budgetService.updateBudget(id, request));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<BudgetDisplayDto> changeStatus(@PathVariable Long id, @RequestParam BudgetStatus status) {
        return ApiResponse.success(budgetService.changeStatus(id, status));
    }
}
