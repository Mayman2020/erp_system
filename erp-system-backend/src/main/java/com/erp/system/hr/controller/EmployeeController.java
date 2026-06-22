package com.erp.system.hr.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.dto.display.EmployeeDisplayDto;
import com.erp.system.hr.dto.form.EmployeeFormDto;
import com.erp.system.hr.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ApiResponse<List<EmployeeDisplayDto>> getAll() {
        return ApiResponse.success(employeeService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<EmployeeDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(employeeService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EmployeeDisplayDto> create(@Valid @RequestBody EmployeeFormDto request) {
        return ApiResponse.success(employeeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<EmployeeDisplayDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeFormDto request) {
        return ApiResponse.success(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ApiResponse.success(null);
    }

}
