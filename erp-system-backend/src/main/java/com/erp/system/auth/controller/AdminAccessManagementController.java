package com.erp.system.auth.controller;

import com.erp.system.auth.dto.AdminAccessContextDto;
import com.erp.system.auth.dto.AdminAccessRoleDto;
import com.erp.system.auth.dto.AdminAccessRoleFormDto;
import com.erp.system.auth.dto.AdminUserDto;
import com.erp.system.auth.dto.AdminUserFormDto;
import com.erp.system.auth.service.AdminAccessManagementService;
import com.erp.system.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/access")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccessManagementController {

    private final AdminAccessManagementService adminAccessManagementService;

    @GetMapping("/context")
    public ApiResponse<AdminAccessContextDto> getContext() {
        return ApiResponse.success(adminAccessManagementService.getContext());
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserDto>> getUsers() {
        return ApiResponse.success(adminAccessManagementService.getUsers());
    }

    @PostMapping("/users")
    public ApiResponse<AdminUserDto> createUser(@Valid @RequestBody AdminUserFormDto request) {
        return ApiResponse.success(adminAccessManagementService.createUser(request));
    }

    @PutMapping("/users/{userId}")
    public ApiResponse<AdminUserDto> updateUser(@PathVariable Long userId, @Valid @RequestBody AdminUserFormDto request) {
        return ApiResponse.success(adminAccessManagementService.updateUser(userId, request));
    }

    @GetMapping("/roles")
    public ApiResponse<List<AdminAccessRoleDto>> getRoles() {
        return ApiResponse.success(adminAccessManagementService.getRoles());
    }

    @PostMapping("/roles")
    public ApiResponse<AdminAccessRoleDto> createRole(@Valid @RequestBody AdminAccessRoleFormDto request) {
        return ApiResponse.success(adminAccessManagementService.createRole(request));
    }

    @PutMapping("/roles/{roleId}")
    public ApiResponse<AdminAccessRoleDto> updateRole(@PathVariable Long roleId, @Valid @RequestBody AdminAccessRoleFormDto request) {
        return ApiResponse.success(adminAccessManagementService.updateRole(roleId, request));
    }
}
