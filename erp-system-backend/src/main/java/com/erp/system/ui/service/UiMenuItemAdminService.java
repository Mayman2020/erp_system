package com.erp.system.ui.service;

import com.erp.system.auth.domain.AccessRole;
import com.erp.system.auth.domain.RoleMenuPermission;
import com.erp.system.auth.repository.AccessRoleRepository;
import com.erp.system.auth.repository.RoleMenuPermissionRepository;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.ui.domain.UiMenuItem;
import com.erp.system.ui.dto.AdminMenuItemFormDto;
import com.erp.system.ui.dto.UiMenuItemAdminDto;
import com.erp.system.ui.repository.UiMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UiMenuItemAdminService {

    private final UiMenuItemRepository uiMenuItemRepository;
    private final AccessRoleRepository accessRoleRepository;
    private final RoleMenuPermissionRepository roleMenuPermissionRepository;

    @Transactional(readOnly = true)
    public List<UiMenuItemAdminDto> listAll() {
        return uiMenuItemRepository.findAllByOrderByParentIdAscSortOrderAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UiMenuItemAdminDto create(AdminMenuItemFormDto request) {
        String id = normalizeId(request.getId());
        if (uiMenuItemRepository.existsById(id)) {
            throw new BusinessException("Menu item id already exists");
        }
        validateParent(request.getParentId());
        UiMenuItem saved = uiMenuItemRepository.save(toEntity(request, id));
        if ("item".equalsIgnoreCase(saved.getItemType())) {
            seedAdminPermissions(saved);
        }
        return toDto(saved);
    }

    @Transactional
    public UiMenuItemAdminDto update(String menuItemId, AdminMenuItemFormDto request) {
        UiMenuItem existing = uiMenuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("UiMenuItem", menuItemId));
        if (!existing.getId().equalsIgnoreCase(normalizeId(request.getId()))) {
            throw new BusinessException("Menu item id cannot be changed");
        }
        validateParent(request.getParentId());
        if (wouldCreateCycle(existing.getId(), request.getParentId())) {
            throw new BusinessException("Invalid parent: would create a cycle");
        }
        applyForm(existing, request);
        return toDto(uiMenuItemRepository.save(existing));
    }

    @Transactional
    public void delete(String menuItemId) {
        if (!uiMenuItemRepository.existsById(menuItemId)) {
            throw new ResourceNotFoundException("UiMenuItem", menuItemId);
        }
        uiMenuItemRepository.deleteById(menuItemId);
    }

    private void validateParent(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return;
        }
        if (!uiMenuItemRepository.existsById(parentId.trim())) {
            throw new BusinessException("Parent menu item not found");
        }
    }

    private boolean wouldCreateCycle(String id, String newParentId) {
        if (newParentId == null || newParentId.isBlank()) {
            return false;
        }
        String cursor = newParentId.trim();
        int guard = 0;
        while (cursor != null && !cursor.isBlank() && guard++ < 64) {
            if (cursor.equalsIgnoreCase(id)) {
                return true;
            }
            UiMenuItem node = uiMenuItemRepository.findById(cursor).orElse(null);
            if (node == null) {
                break;
            }
            cursor = node.getParentId();
        }
        return false;
    }

    private void seedAdminPermissions(UiMenuItem menuItem) {
        AccessRole admin = accessRoleRepository.findByCodeIgnoreCase("ADMIN").orElse(null);
        if (admin == null) {
            return;
        }
        if (roleMenuPermissionRepository.existsByRole_IdAndMenuItem_Id(admin.getId(), menuItem.getId())) {
            return;
        }
        roleMenuPermissionRepository.save(RoleMenuPermission.builder()
                .role(admin)
                .menuItem(menuItem)
                .canView(true)
                .canCreate(true)
                .canEdit(true)
                .canDelete(true)
                .build());
    }

    private UiMenuItem toEntity(AdminMenuItemFormDto request, String id) {
        UiMenuItem item = new UiMenuItem();
        item.setId(id);
        applyForm(item, request);
        return item;
    }

    private void applyForm(UiMenuItem item, AdminMenuItemFormDto request) {
        String parent = request.getParentId();
        item.setParentId(parent == null || parent.isBlank() ? null : parent.trim());
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        item.setItemType(request.getItemType() == null ? "item" : request.getItemType().trim());
        item.setTitleKey(requireText(request.getTitleKey(), "titleKey"));
        item.setIcon(trimToNull(request.getIcon()));
        item.setUrl(trimToNull(request.getUrl()));
        item.setExternal(Boolean.TRUE.equals(request.getExternal()));
        item.setTargetBlank(Boolean.TRUE.equals(request.getTargetBlank()));
        item.setRolesCsv(trimToNull(request.getRolesCsv()));
        item.setItemClasses(trimToNull(request.getItemClasses()));
        item.setBreadcrumbsFlag(request.getBreadcrumbsFlag());
    }

    private UiMenuItemAdminDto toDto(UiMenuItem item) {
        return UiMenuItemAdminDto.builder()
                .id(item.getId())
                .parentId(item.getParentId())
                .sortOrder(item.getSortOrder())
                .itemType(item.getItemType())
                .titleKey(item.getTitleKey())
                .icon(item.getIcon())
                .url(item.getUrl())
                .external(item.getExternal())
                .targetBlank(item.getTargetBlank())
                .rolesCsv(item.getRolesCsv())
                .itemClasses(item.getItemClasses())
                .breadcrumbsFlag(item.getBreadcrumbsFlag())
                .build();
    }

    private static String normalizeId(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("Id is required");
        }
        return value.trim();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(field + " is required");
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }
}
