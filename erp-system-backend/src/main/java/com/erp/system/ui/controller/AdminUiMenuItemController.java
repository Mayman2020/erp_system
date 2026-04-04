package com.erp.system.ui.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.ui.dto.AdminMenuItemFormDto;
import com.erp.system.ui.dto.UiMenuItemAdminDto;
import com.erp.system.ui.service.UiMenuItemAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/ui/menu-items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUiMenuItemController {

    private final UiMenuItemAdminService uiMenuItemAdminService;

    @GetMapping
    public ApiResponse<List<UiMenuItemAdminDto>> list() {
        return ApiResponse.success(uiMenuItemAdminService.listAll());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UiMenuItemAdminDto> create(@Valid @RequestBody AdminMenuItemFormDto request) {
        return ApiResponse.success(uiMenuItemAdminService.create(request));
    }

    @PutMapping("/{menuItemId}")
    public ApiResponse<UiMenuItemAdminDto> update(
            @PathVariable String menuItemId,
            @Valid @RequestBody AdminMenuItemFormDto request
    ) {
        request.setId(menuItemId);
        return ApiResponse.success(uiMenuItemAdminService.update(menuItemId, request));
    }

    @DeleteMapping("/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable String menuItemId) {
        uiMenuItemAdminService.delete(menuItemId);
        return ApiResponse.success(null);
    }
}
