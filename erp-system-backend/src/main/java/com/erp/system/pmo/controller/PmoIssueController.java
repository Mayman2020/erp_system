package com.erp.system.pmo.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.pmo.dto.display.PmoIssueDisplayDto;
import com.erp.system.pmo.dto.form.PmoIssueFormDto;
import com.erp.system.pmo.service.PmoIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pmo/projects/{projectId}/issues")
@RequiredArgsConstructor
public class PmoIssueController {

    private final PmoIssueService pmoIssueService;

    @GetMapping
    public ApiResponse<List<PmoIssueDisplayDto>> getAll(@PathVariable Long projectId) {
        return ApiResponse.success(pmoIssueService.getByProject(projectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PmoIssueDisplayDto> getById(@PathVariable Long projectId, @PathVariable Long id) {
        return ApiResponse.success(pmoIssueService.getById(projectId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PmoIssueDisplayDto> create(@PathVariable Long projectId, @Valid @RequestBody PmoIssueFormDto request) {
        return ApiResponse.success(pmoIssueService.create(projectId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PmoIssueDisplayDto> update(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody PmoIssueFormDto request) {
        return ApiResponse.success(pmoIssueService.update(projectId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        pmoIssueService.delete(projectId, id);
        return ApiResponse.success(null);
    }
}
