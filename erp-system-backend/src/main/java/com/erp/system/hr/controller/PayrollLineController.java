package com.erp.system.hr.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.dto.display.PayrollLineDisplayDto;
import com.erp.system.hr.dto.form.PayrollLineFormDto;
import com.erp.system.hr.service.PayrollLineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/payroll-lines")
@RequiredArgsConstructor
public class PayrollLineController {

    private final PayrollLineService payrollLineService;

    @GetMapping
    public ApiResponse<List<PayrollLineDisplayDto>> getAll() {
        return ApiResponse.success(payrollLineService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PayrollLineDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(payrollLineService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PayrollLineDisplayDto> create(@Valid @RequestBody PayrollLineFormDto request) {
        return ApiResponse.success(payrollLineService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PayrollLineDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PayrollLineFormDto request) {
        return ApiResponse.success(payrollLineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        payrollLineService.delete(id);
        return ApiResponse.success(null);
    }

}
