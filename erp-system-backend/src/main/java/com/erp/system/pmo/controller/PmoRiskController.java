package com.erp.system.pmo.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.pmo.dto.display.PmoRiskDisplayDto;
import com.erp.system.pmo.dto.form.PmoRiskFormDto;
import com.erp.system.pmo.service.PmoRiskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pmo/projects/{projectId}/risks")
@RequiredArgsConstructor
public class PmoRiskController {

    private final PmoRiskService pmoRiskService;

    @GetMapping
    public ApiResponse<List<PmoRiskDisplayDto>> getAll(@PathVariable Long projectId) {
        return ApiResponse.success(pmoRiskService.getByProject(projectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PmoRiskDisplayDto> getById(@PathVariable Long projectId, @PathVariable Long id) {
        return ApiResponse.success(pmoRiskService.getById(projectId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PmoRiskDisplayDto> create(@PathVariable Long projectId, @Valid @RequestBody PmoRiskFormDto request) {
        return ApiResponse.success(pmoRiskService.create(projectId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PmoRiskDisplayDto> update(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody PmoRiskFormDto request) {
        return ApiResponse.success(pmoRiskService.update(projectId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        pmoRiskService.delete(projectId, id);
        return ApiResponse.success(null);
    }
}
