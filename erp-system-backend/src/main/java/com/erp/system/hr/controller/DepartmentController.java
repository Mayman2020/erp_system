package com.erp.system.hr.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.dto.display.DepartmentDisplayDto;
import com.erp.system.hr.dto.form.DepartmentFormDto;
import com.erp.system.hr.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<DepartmentDisplayDto>> getAll() {
        return ApiResponse.success(departmentService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(departmentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DepartmentDisplayDto> create(@Valid @RequestBody DepartmentFormDto request) {
        return ApiResponse.success(departmentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentDisplayDto> update(@PathVariable Long id, @Valid @RequestBody DepartmentFormDto request) {
        return ApiResponse.success(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.success(null);
    }

}
