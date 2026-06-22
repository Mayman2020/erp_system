package com.erp.system.projects.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.projects.dto.display.ProjectExpenseDisplayDto;
import com.erp.system.projects.dto.form.ProjectExpenseFormDto;
import com.erp.system.projects.service.ProjectExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/expenses")
@RequiredArgsConstructor
public class ProjectExpenseController {

    private final ProjectExpenseService projectExpenseService;

    @GetMapping
    public ApiResponse<List<ProjectExpenseDisplayDto>> getAll() {
        return ApiResponse.success(projectExpenseService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectExpenseDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(projectExpenseService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectExpenseDisplayDto> create(@Valid @RequestBody ProjectExpenseFormDto request) {
        return ApiResponse.success(projectExpenseService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectExpenseDisplayDto> update(@PathVariable Long id, @Valid @RequestBody ProjectExpenseFormDto request) {
        return ApiResponse.success(projectExpenseService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<ProjectExpenseDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(projectExpenseService.approve(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ProjectExpenseDisplayDto> cancel(@PathVariable Long id,
                                                        @RequestParam String actor,
                                                        @RequestParam(required = false) String reason) {
        return ApiResponse.success(projectExpenseService.cancel(id, actor, reason));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        projectExpenseService.delete(id);
        return ApiResponse.success(null);
    }

}
