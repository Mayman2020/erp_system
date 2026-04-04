package com.erp.system.auth.service;

import com.erp.system.auth.domain.AccessRole;
import com.erp.system.auth.domain.RoleMenuPermission;
import com.erp.system.auth.domain.User;
import com.erp.system.auth.domain.UserAccessRole;
import com.erp.system.auth.dto.AdminAccessContextDto;
import com.erp.system.auth.dto.AdminAccessRoleDto;
import com.erp.system.auth.dto.AdminAccessRoleFormDto;
import com.erp.system.auth.dto.AdminAccessRolePermissionDto;
import com.erp.system.auth.dto.AdminAccessRolePermissionFormDto;
import com.erp.system.auth.dto.AdminUserDto;
import com.erp.system.auth.dto.AdminUserFormDto;
import com.erp.system.auth.repository.AccessRoleRepository;
import com.erp.system.auth.repository.RoleMenuPermissionRepository;
import com.erp.system.auth.repository.UserAccessRoleRepository;
import com.erp.system.auth.repository.UserRepository;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.ui.domain.UiMenuItem;
import com.erp.system.ui.dto.AdminMenuItemDto;
import com.erp.system.ui.repository.UiMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAccessManagementService {

    private final UserRepository userRepository;
    private final AccessRoleRepository accessRoleRepository;
    private final UserAccessRoleRepository userAccessRoleRepository;
    private final RoleMenuPermissionRepository roleMenuPermissionRepository;
    private final UiMenuItemRepository uiMenuItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AdminAccessContextDto getContext() {
        return AdminAccessContextDto.builder()
                .users(getUsers())
                .roles(getRoles())
                .menuItems(getMenuItems())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminUserDto> getUsers() {
        return userRepository.findAllByOrderByUsernameAsc().stream()
                .map(this::toUserDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminAccessRoleDto> getRoles() {
        Map<String, UiMenuItem> menuIndex = uiMenuItemRepository.findAllByOrderByParentIdAscSortOrderAsc().stream()
                .filter(item -> "item".equalsIgnoreCase(item.getItemType()))
                .collect(Collectors.toMap(UiMenuItem::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        return accessRoleRepository.findAllByOrderBySystemRoleDescCodeAsc().stream()
                .map(role -> toRoleDto(role, menuIndex))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminMenuItemDto> getMenuItems() {
        return uiMenuItemRepository.findAllByOrderByParentIdAscSortOrderAsc().stream()
                .filter(item -> "item".equalsIgnoreCase(item.getItemType()))
                .map(item -> AdminMenuItemDto.builder()
                        .id(item.getId())
                        .parentId(item.getParentId())
                        .sortOrder(item.getSortOrder())
                        .itemType(item.getItemType())
                        .titleKey(item.getTitleKey())
                        .icon(item.getIcon())
                        .url(item.getUrl())
                        .build())
                .toList();
    }

    @Transactional
    public AdminUserDto createUser(AdminUserFormDto request) {
        validateUser(request, null);

        User user = User.builder()
                .username(normalize(request.getUsername()))
                .email(normalizeEmail(request.getEmail()))
                .phone(normalize(request.getPhone()))
                .password(passwordEncoder.encode(normalizePassword(request.getPassword(), true)))
                .role(request.getPrimaryRole())
                .active(Boolean.TRUE.equals(request.getActive()))
                .build();

        user.setProfile(com.erp.system.auth.domain.UserProfile.builder()
                .fullName(normalize(request.getFullName()))
                .build());

        User saved = userRepository.save(user);
        syncAssignments(saved, request.getRoleIds());
        return toUserDto(saved);
    }

    @Transactional
    public AdminUserDto setUserActive(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setActive(active);
        return toUserDto(userRepository.save(user));
    }

    @Transactional
    public void deleteRole(Long roleId) {
        AccessRole role = accessRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("AccessRole", roleId));
        if (role.isSystemRole()) {
            throw new BusinessException("System roles cannot be deleted");
        }
        userAccessRoleRepository.deleteByRoleId(roleId);
        roleMenuPermissionRepository.deleteByRoleId(roleId);
        accessRoleRepository.delete(role);
    }

    @Transactional
    public AdminUserDto updateUser(Long userId, AdminUserFormDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        validateUser(request, userId);

        user.setUsername(normalize(request.getUsername()));
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setPhone(normalize(request.getPhone()));
        user.setRole(request.getPrimaryRole());
        user.setActive(Boolean.TRUE.equals(request.getActive()));
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(normalizePassword(request.getPassword(), false)));
        }

        if (user.getProfile() == null) {
            user.setProfile(com.erp.system.auth.domain.UserProfile.builder().build());
        }
        user.getProfile().setFullName(normalize(request.getFullName()));

        User saved = userRepository.save(user);
        syncAssignments(saved, request.getRoleIds());
        return toUserDto(saved);
    }

    @Transactional
    public AdminAccessRoleDto createRole(AdminAccessRoleFormDto request) {
        validateRole(request, null);
        AccessRole role = AccessRole.builder()
                .code(normalizeRoleCode(request.getCode()))
                .nameEn(normalize(request.getNameEn()))
                .nameAr(normalize(request.getNameAr()))
                .active(Boolean.TRUE.equals(request.getActive()))
                .systemRole(false)
                .build();
        AccessRole saved = accessRoleRepository.save(role);
        syncPermissions(saved, request.getPermissions());
        return toRoleDto(saved, indexMenuItems());
    }

    @Transactional
    public AdminAccessRoleDto updateRole(Long roleId, AdminAccessRoleFormDto request) {
        AccessRole role = accessRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("AccessRole", roleId));
        validateRole(request, roleId);

        String requestedCode = normalizeRoleCode(request.getCode());
        if (role.isSystemRole() && !role.getCode().equalsIgnoreCase(requestedCode)) {
            throw new BusinessException("System role code cannot be changed");
        }

        role.setCode(requestedCode);
        role.setNameEn(normalize(request.getNameEn()));
        role.setNameAr(normalize(request.getNameAr()));
        role.setActive(Boolean.TRUE.equals(request.getActive()));

        AccessRole saved = accessRoleRepository.save(role);
        syncPermissions(saved, request.getPermissions());
        return toRoleDto(saved, indexMenuItems());
    }

    private void syncAssignments(User user, List<Long> roleIds) {
        userAccessRoleRepository.deleteByUserId(user.getId());
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }

        List<AccessRole> roles = accessRoleRepository.findByIdIn(roleIds);
        if (roles.size() != roleIds.stream().filter(id -> id != null).collect(Collectors.toSet()).size()) {
            throw new BusinessException("One or more selected roles were not found");
        }

        for (AccessRole role : roles) {
            userAccessRoleRepository.save(UserAccessRole.builder()
                    .user(user)
                    .role(role)
                    .build());
        }
    }

    private void syncPermissions(AccessRole role, List<AdminAccessRolePermissionFormDto> permissions) {
        roleMenuPermissionRepository.deleteByRoleId(role.getId());
        if (permissions == null || permissions.isEmpty()) {
            throw new BusinessException("Role must contain at least one permission row");
        }

        Map<String, UiMenuItem> menus = indexMenuItems();
        for (AdminAccessRolePermissionFormDto permission : permissions) {
            UiMenuItem menu = menus.get(permission.getMenuItemId());
            if (menu == null) {
                throw new BusinessException("Menu item not found: " + permission.getMenuItemId());
            }

            boolean canView = Boolean.TRUE.equals(permission.getCanView());
            boolean canCreate = Boolean.TRUE.equals(permission.getCanCreate());
            boolean canEdit = Boolean.TRUE.equals(permission.getCanEdit());
            boolean canDelete = Boolean.TRUE.equals(permission.getCanDelete());
            if (!canView && (canCreate || canEdit || canDelete)) {
                canView = true;
            }

            roleMenuPermissionRepository.save(RoleMenuPermission.builder()
                    .role(role)
                    .menuItem(menu)
                    .canView(canView)
                    .canCreate(canCreate)
                    .canEdit(canEdit)
                    .canDelete(canDelete)
                    .build());
        }
    }

    private AdminUserDto toUserDto(User user) {
        List<AccessRole> assignedRoles = userAccessRoleRepository.findByUserIdOrderByRoleCodeAsc(user.getId()).stream()
                .map(UserAccessRole::getRole)
                .toList();
        return AdminUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .primaryRole(user.getRole() == null ? null : user.getRole().name())
                .active(user.isActive())
                .fullName(user.getProfile() == null ? null : user.getProfile().getFullName())
                .createdAt(user.getCreatedAt())
                .roleIds(assignedRoles.stream().map(AccessRole::getId).toList())
                .roleCodes(assignedRoles.stream().map(AccessRole::getCode).toList())
                .build();
    }

    private AdminAccessRoleDto toRoleDto(AccessRole role, Map<String, UiMenuItem> menuIndex) {
        Map<String, RoleMenuPermission> permissions = roleMenuPermissionRepository.findByRoleIdOrderByMenuItemSortOrderAscMenuItemIdAsc(role.getId()).stream()
                .collect(Collectors.toMap(permission -> permission.getMenuItem().getId(), permission -> permission, (left, right) -> left));

        List<AdminAccessRolePermissionDto> items = menuIndex.values().stream()
                .sorted(Comparator.comparing(UiMenuItem::getSortOrder).thenComparing(UiMenuItem::getId))
                .map(menu -> {
                    RoleMenuPermission permission = permissions.get(menu.getId());
                    return AdminAccessRolePermissionDto.builder()
                            .menuItemId(menu.getId())
                            .titleKey(menu.getTitleKey())
                            .url(menu.getUrl())
                            .itemType(menu.getItemType())
                            .parentId(menu.getParentId())
                            .sortOrder(menu.getSortOrder())
                            .canView(permission != null && permission.isCanView())
                            .canCreate(permission != null && permission.isCanCreate())
                            .canEdit(permission != null && permission.isCanEdit())
                            .canDelete(permission != null && permission.isCanDelete())
                            .build();
                })
                .toList();

        return AdminAccessRoleDto.builder()
                .id(role.getId())
                .code(role.getCode())
                .nameEn(role.getNameEn())
                .nameAr(role.getNameAr())
                .active(role.isActive())
                .systemRole(role.isSystemRole())
                .permissions(items)
                .build();
    }

    private Map<String, UiMenuItem> indexMenuItems() {
        return uiMenuItemRepository.findAllByOrderByParentIdAscSortOrderAsc().stream()
                .filter(item -> "item".equalsIgnoreCase(item.getItemType()))
                .collect(Collectors.toMap(UiMenuItem::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private void validateUser(AdminUserFormDto request, Long userId) {
        String username = normalize(request.getUsername());
        String email = normalizeEmail(request.getEmail());
        if (userId == null) {
            if (userRepository.existsByUsernameIgnoreCase(username)) {
                throw new BusinessException("AUTH.ERRORS.USERNAME_IN_USE");
            }
            if (userRepository.existsByEmailIgnoreCase(email)) {
                throw new BusinessException("AUTH.ERRORS.EMAIL_IN_USE");
            }
            normalizePassword(request.getPassword(), true);
            return;
        }

        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(username, userId)) {
            throw new BusinessException("AUTH.ERRORS.USERNAME_IN_USE");
        }
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new BusinessException("AUTH.ERRORS.EMAIL_IN_USE");
        }
        normalizePassword(request.getPassword(), false);
    }

    private void validateRole(AdminAccessRoleFormDto request, Long roleId) {
        String code = normalizeRoleCode(request.getCode());
        if (roleId == null) {
            if (accessRoleRepository.existsByCodeIgnoreCase(code)) {
                throw new BusinessException("Role code already exists");
            }
            return;
        }
        if (accessRoleRepository.existsByCodeIgnoreCaseAndIdNot(code, roleId)) {
            throw new BusinessException("Role code already exists");
        }
    }

    private String normalize(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new BusinessException("AUTH.ERRORS.INVALID_REQUEST");
        }
        return normalized;
    }

    private String normalizeEmail(String value) {
        return normalize(value).toLowerCase(Locale.ROOT);
    }

    private String normalizeRoleCode(String value) {
        return normalize(value).replace(' ', '_').toUpperCase(Locale.ROOT);
    }

    private String normalizeOptional(String value) {
        String normalized = value == null ? null : value.trim();
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    private String normalizePassword(String password, boolean required) {
        String normalized = password == null ? "" : password.trim();
        if (!required && normalized.isBlank()) {
            return normalized;
        }
        if (normalized.length() < 8) {
            throw new BusinessException("AUTH.REGISTER.PASSWORD_WEAK");
        }
        return normalized;
    }
}
