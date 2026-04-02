package com.erp.system.ui.controller;

import com.erp.system.auth.service.AccessControlService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.ui.dto.MenuActionPermissionDto;
import com.erp.system.ui.dto.MenuNodeDto;
import com.erp.system.ui.service.UiMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ui/menu")
@RequiredArgsConstructor
public class UiMenuController {

    private final UiMenuService uiMenuService;
    private final AccessControlService accessControlService;

    @GetMapping
    public ApiResponse<List<MenuNodeDto>> getMenu(Authentication authentication) {
        return ApiResponse.success(uiMenuService.getMenuForUser(authentication));
    }

    @GetMapping("/permissions")
    public ApiResponse<List<MenuActionPermissionDto>> getPermissions(Authentication authentication) {
        return ApiResponse.success(accessControlService.menuPermissions(authentication));
    }
}
