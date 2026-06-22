package com.erp.system.projects.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.projects.dto.display.ProjectMemberDisplayDto;
import com.erp.system.projects.dto.form.ProjectMemberFormDto;
import com.erp.system.projects.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @GetMapping
    public ApiResponse<List<ProjectMemberDisplayDto>> getAll() {
        return ApiResponse.success(projectMemberService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectMemberDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(projectMemberService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProjectMemberDisplayDto> create(@Valid @RequestBody ProjectMemberFormDto request) {
        return ApiResponse.success(projectMemberService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectMemberDisplayDto> update(@PathVariable Long id, @Valid @RequestBody ProjectMemberFormDto request) {
        return ApiResponse.success(projectMemberService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        projectMemberService.delete(id);
        return ApiResponse.success(null);
    }

}
