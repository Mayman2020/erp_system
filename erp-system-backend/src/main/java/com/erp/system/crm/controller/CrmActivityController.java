package com.erp.system.crm.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.crm.dto.display.CrmActivityDisplayDto;
import com.erp.system.crm.dto.form.CrmActivityFormDto;
import com.erp.system.crm.service.CrmActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crm/activities")
@RequiredArgsConstructor
public class CrmActivityController {

    private final CrmActivityService crmActivityService;

    @GetMapping
    public ApiResponse<List<CrmActivityDisplayDto>> getAll() {
        return ApiResponse.success(crmActivityService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<CrmActivityDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(crmActivityService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CrmActivityDisplayDto> create(@Valid @RequestBody CrmActivityFormDto request) {
        return ApiResponse.success(crmActivityService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CrmActivityDisplayDto> update(@PathVariable Long id, @Valid @RequestBody CrmActivityFormDto request) {
        return ApiResponse.success(crmActivityService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        crmActivityService.delete(id);
        return ApiResponse.success(null);
    }

}
