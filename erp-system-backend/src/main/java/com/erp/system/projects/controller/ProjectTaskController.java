package com.erp.system.projects.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.projects.dto.display.ProjectTaskDisplayDto;
import com.erp.system.projects.dto.form.ProjectTaskFormDto;
import com.erp.system.projects.service.ProjectTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/tasks")
@RequiredArgsConstructor
public class ProjectTaskController {

    private final ProjectTaskService projectTaskService;

    @GetMapping
    public ApiResponse<List<ProjectTaskDisplayDto>> getAll() {
        return ApiResponse.success(projectTaskService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectTaskDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(projectTaskService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectTaskDisplayDto> create(@Valid @RequestBody ProjectTaskFormDto request) {
        return ApiResponse.success(projectTaskService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectTaskDisplayDto> update(@PathVariable Long id, @Valid @RequestBody ProjectTaskFormDto request) {
        return ApiResponse.success(projectTaskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        projectTaskService.delete(id);
        return ApiResponse.success(null);
    }

}
