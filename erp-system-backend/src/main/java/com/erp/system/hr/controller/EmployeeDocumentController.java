package com.erp.system.hr.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.dto.display.EmployeeDocumentDisplayDto;
import com.erp.system.hr.dto.form.EmployeeDocumentFormDto;
import com.erp.system.hr.service.EmployeeDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/documents")
@RequiredArgsConstructor
public class EmployeeDocumentController {

    private final EmployeeDocumentService employeeDocumentService;

    @GetMapping
    public ApiResponse<List<EmployeeDocumentDisplayDto>> getAll() {
        return ApiResponse.success(employeeDocumentService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<EmployeeDocumentDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(employeeDocumentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EmployeeDocumentDisplayDto> create(@Valid @RequestBody EmployeeDocumentFormDto request) {
        return ApiResponse.success(employeeDocumentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<EmployeeDocumentDisplayDto> update(@PathVariable Long id, @Valid @RequestBody EmployeeDocumentFormDto request) {
        return ApiResponse.success(employeeDocumentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        employeeDocumentService.delete(id);
        return ApiResponse.success(null);
    }

}
