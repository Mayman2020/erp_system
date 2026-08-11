package com.erp.system.pmo.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.pmo.dto.display.PmoMilestoneDisplayDto;
import com.erp.system.pmo.dto.form.PmoMilestoneFormDto;
import com.erp.system.pmo.service.PmoMilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pmo/projects/{projectId}/milestones")
@RequiredArgsConstructor
public class PmoMilestoneController {

    private final PmoMilestoneService pmoMilestoneService;

    @GetMapping
    public ApiResponse<List<PmoMilestoneDisplayDto>> getAll(@PathVariable Long projectId) {
        return ApiResponse.success(pmoMilestoneService.getByProject(projectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PmoMilestoneDisplayDto> getById(@PathVariable Long projectId, @PathVariable Long id) {
        return ApiResponse.success(pmoMilestoneService.getById(projectId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PmoMilestoneDisplayDto> create(@PathVariable Long projectId, @Valid @RequestBody PmoMilestoneFormDto request) {
        return ApiResponse.success(pmoMilestoneService.create(projectId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PmoMilestoneDisplayDto> update(@PathVariable Long projectId, @PathVariable Long id, @Valid @RequestBody PmoMilestoneFormDto request) {
        return ApiResponse.success(pmoMilestoneService.update(projectId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        pmoMilestoneService.delete(projectId, id);
        return ApiResponse.success(null);
    }
}
