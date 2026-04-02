package com.erp.system.auth.service;

import com.erp.system.auth.domain.AccessRole;
import com.erp.system.auth.domain.RoleMenuPermission;
import com.erp.system.auth.domain.User;
import com.erp.system.auth.repository.AccessRoleRepository;
import com.erp.system.auth.repository.RoleMenuPermissionRepository;
import com.erp.system.auth.repository.UserAccessRoleRepository;
import com.erp.system.common.security.JwtPrincipal;
import com.erp.system.ui.domain.UiMenuItem;
import com.erp.system.ui.dto.MenuActionPermissionDto;
import com.erp.system.ui.repository.UiMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final UserAccessRoleRepository userAccessRoleRepository;
    private final AccessRoleRepository accessRoleRepository;
    private final RoleMenuPermissionRepository roleMenuPermissionRepository;
    private final UiMenuItemRepository uiMenuItemRepository;

    @Transactional(readOnly = true)
    public List<AccessRole> assignedRoles(Long userId) {
        return userAccessRoleRepository.findByUserIdOrderByRoleCodeAsc(userId).stream()
                .map(assignment -> assignment.getRole())
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasCustomAssignments(Long userId) {
        return userAccessRoleRepository.existsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<String> authorityCodesFor(User user) {
        Set<String> codes = new LinkedHashSet<>();
        if (user != null) {
            assignedRoles(user.getId()).stream()
                    .map(AccessRole::getCode)
                    .forEach(codes::add);
            if (user.getRole() != null) {
                codes.add(user.getRole().name());
            }
        }
        return new ArrayList<>(codes);
    }

    @Transactional(readOnly = true)
    public List<MenuActionPermissionDto> menuPermissions(Authentication authentication) {
        Long userId = currentUserId(authentication);
        if (userId == null) {
            return List.of();
        }

        if (hasCustomAssignments(userId)) {
            List<String> roleCodes = assignedRoles(userId).stream()
                    .map(AccessRole::getCode)
                    .toList();
            return mergePermissions(roleMenuPermissionRepository.findByRoleCodeIn(roleCodes));
        }

        return fallbackPermissions(authorityCodes(authentication));
    }

    @Transactional(readOnly = true)
    public boolean hasMenuAction(Authentication authentication, String menuItemId, String action) {
        if (menuItemId == null || menuItemId.isBlank()) {
            return false;
        }
        return menuPermissions(authentication).stream()
                .filter(permission -> menuItemId.equalsIgnoreCase(permission.menuItemId()))
                .findFirst()
                .map(permission -> switch ((action == null ? "" : action).trim().toUpperCase(Locale.ROOT)) {
                    case "VIEW" -> permission.canView();
                    case "CREATE" -> permission.canCreate();
                    case "EDIT" -> permission.canEdit();
                    case "DELETE" -> permission.canDelete();
                    default -> false;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<String> currentRoleCodes(Authentication authentication) {
        Long userId = currentUserId(authentication);
        if (userId == null) {
            return authorityCodes(authentication);
        }
        if (hasCustomAssignments(userId)) {
            return assignedRoles(userId).stream()
                    .map(AccessRole::getCode)
                    .toList();
        }
        return authorityCodes(authentication);
    }

    private List<MenuActionPermissionDto> mergePermissions(List<RoleMenuPermission> rows) {
        Map<String, MenuActionPermissionDto.MenuActionPermissionDtoBuilder> merged = new LinkedHashMap<>();
        for (RoleMenuPermission row : rows) {
            String menuId = row.getMenuItem().getId();
            MenuActionPermissionDto.MenuActionPermissionDtoBuilder current = merged.computeIfAbsent(menuId,
                    ignored -> MenuActionPermissionDto.builder()
                            .menuItemId(menuId)
                            .canView(false)
                            .canCreate(false)
                            .canEdit(false)
                            .canDelete(false));

            MenuActionPermissionDto snapshot = current.build();
            merged.put(menuId, MenuActionPermissionDto.builder()
                    .menuItemId(menuId)
                    .canView(snapshot.canView() || row.isCanView())
                    .canCreate(snapshot.canCreate() || row.isCanCreate())
                    .canEdit(snapshot.canEdit() || row.isCanEdit())
                    .canDelete(snapshot.canDelete() || row.isCanDelete()));
        }

        return merged.values().stream().map(MenuActionPermissionDto.MenuActionPermissionDtoBuilder::build).toList();
    }

    private List<MenuActionPermissionDto> fallbackPermissions(Collection<String> roleCodes) {
        Set<String> normalizedRoles = roleCodes.stream()
                .map(code -> code == null ? "" : code.trim().toUpperCase(Locale.ROOT))
                .filter(code -> !code.isBlank())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        return uiMenuItemRepository.findAllByOrderByParentIdAscSortOrderAsc().stream()
                .filter(menu -> canSeeFallback(menu, normalizedRoles))
                .filter(menu -> "item".equalsIgnoreCase(menu.getItemType()))
                .map(menu -> MenuActionPermissionDto.builder()
                        .menuItemId(menu.getId())
                        .canView(true)
                        .canCreate(true)
                        .canEdit(true)
                        .canDelete(true)
                        .build())
                .toList();
    }

    private boolean canSeeFallback(UiMenuItem row, Set<String> normalizedRoles) {
        String csv = row.getRolesCsv();
        if (csv == null || csv.isBlank()) {
            return true;
        }
        for (String value : csv.split(",")) {
            String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
            if (!normalized.isBlank() && normalizedRoles.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            return null;
        }
        return principal.userId();
    }

    private List<String> authorityCodes(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return List.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(code -> code.startsWith("ROLE_") ? code.substring(5) : code)
                .toList();
    }
}
