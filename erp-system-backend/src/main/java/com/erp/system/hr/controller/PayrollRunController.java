package com.erp.system.hr.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.dto.display.PayrollRunDisplayDto;
import com.erp.system.hr.dto.form.PayrollRunFormDto;
import com.erp.system.hr.service.PayrollRunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/payroll")
@RequiredArgsConstructor
public class PayrollRunController {

    private final PayrollRunService payrollRunService;

    @GetMapping
    public ApiResponse<List<PayrollRunDisplayDto>> getAll() {
        return ApiResponse.success(payrollRunService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PayrollRunDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(payrollRunService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PayrollRunDisplayDto> create(@Valid @RequestBody PayrollRunFormDto request) {
        return ApiResponse.success(payrollRunService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PayrollRunDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PayrollRunFormDto request) {
        return ApiResponse.success(payrollRunService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<PayrollRunDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(payrollRunService.approve(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<PayrollRunDisplayDto> cancel(@PathVariable Long id,
                                                    @RequestParam String actor,
                                                    @RequestParam(required = false) String reason) {
        return ApiResponse.success(payrollRunService.cancel(id, actor, reason));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        payrollRunService.delete(id);
        return ApiResponse.success(null);
    }
}
