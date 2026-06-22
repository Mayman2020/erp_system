package com.erp.system.projects.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.projects.dto.display.ProjectDisplayDto;
import com.erp.system.projects.dto.form.ProjectFormDto;
import com.erp.system.projects.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ApiResponse<List<ProjectDisplayDto>> getAll() {
        return ApiResponse.success(projectService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(projectService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectDisplayDto> create(@Valid @RequestBody ProjectFormDto request) {
        return ApiResponse.success(projectService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectDisplayDto> update(@PathVariable Long id, @Valid @RequestBody ProjectFormDto request) {
        return ApiResponse.success(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ApiResponse.success(null);
    }

}
