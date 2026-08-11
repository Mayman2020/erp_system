package com.erp.system.auth.service;

import com.erp.system.auth.domain.AccessRole;
import com.erp.system.auth.domain.RoleMenuPermission;
import com.erp.system.auth.domain.UserAccessRole;
import com.erp.system.auth.repository.AccessRoleRepository;
import com.erp.system.auth.repository.RoleMenuPermissionRepository;
import com.erp.system.auth.repository.UserAccessRoleRepository;
import com.erp.system.common.security.ActiveRoleContext;
import com.erp.system.common.security.JwtPrincipal;
import com.erp.system.ui.domain.UiMenuItem;
import com.erp.system.ui.repository.UiMenuItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessControlServiceActiveRoleTest {

    @Mock private UserAccessRoleRepository userAccessRoleRepository;
    @Mock private AccessRoleRepository accessRoleRepository;
    @Mock private RoleMenuPermissionRepository roleMenuPermissionRepository;
    @Mock private UiMenuItemRepository uiMenuItemRepository;
    @Mock private UserAccessRole assignment;
    @Mock private AccessRole assignedRole;
    @Mock private RoleMenuPermission permissionRow;
    @Mock private UiMenuItem menuItem;

    @Test
    void activeRoleUsesOnlyThatRolesPermissions() {
        ActiveRoleContext context = new ActiveRoleContext();
        context.setRoleCode("REPORT_VIEWER");
        AccessControlService service = new AccessControlService(
                userAccessRoleRepository,
                accessRoleRepository,
                roleMenuPermissionRepository,
                uiMenuItemRepository,
                context
        );
        var authentication = new UsernamePasswordAuthenticationToken(
                new JwtPrincipal(12L, "viewer"),
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_REPORT_VIEWER")
                )
        );

        when(userAccessRoleRepository.existsByUserId(12L)).thenReturn(true);
        when(userAccessRoleRepository.findByUserIdOrderByRoleCodeAsc(12L)).thenReturn(List.of(assignment));
        when(assignment.getRole()).thenReturn(assignedRole);
        when(assignedRole.getCode()).thenReturn("REPORT_VIEWER");
        when(roleMenuPermissionRepository.findByRoleCodeIn(List.of("REPORT_VIEWER"))).thenReturn(List.of(permissionRow));
        when(permissionRow.getMenuItem()).thenReturn(menuItem);
        when(menuItem.getId()).thenReturn("erp-reports");
        when(permissionRow.isCanView()).thenReturn(true);

        var permissions = service.menuPermissions(authentication);

        assertTrue(permissions.get(0).canView());
        assertFalse(permissions.get(0).canCreate());
        verify(roleMenuPermissionRepository).findByRoleCodeIn(List.of("REPORT_VIEWER"));
    }
}
