package com.erp.system.crm.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.crm.dto.display.CrmLeadDisplayDto;
import com.erp.system.crm.dto.form.CrmLeadFormDto;
import com.erp.system.crm.service.CrmLeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crm/leads")
@RequiredArgsConstructor
public class CrmLeadController {

    private final CrmLeadService crmLeadService;

    @GetMapping
    public ApiResponse<List<CrmLeadDisplayDto>> getAll() {
        return ApiResponse.success(crmLeadService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<CrmLeadDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(crmLeadService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CrmLeadDisplayDto> create(@Valid @RequestBody CrmLeadFormDto request) {
        return ApiResponse.success(crmLeadService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CrmLeadDisplayDto> update(@PathVariable Long id, @Valid @RequestBody CrmLeadFormDto request) {
        return ApiResponse.success(crmLeadService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        crmLeadService.delete(id);
        return ApiResponse.success(null);
    }

}
