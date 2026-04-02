package com.erp.system.auth.repository;

import com.erp.system.auth.domain.RoleMenuPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoleMenuPermissionRepository extends JpaRepository<RoleMenuPermission, Long> {

    List<RoleMenuPermission> findByRoleIdOrderByMenuItemSortOrderAscMenuItemIdAsc(Long roleId);

    List<RoleMenuPermission> findByRoleCodeIn(Collection<String> roleCodes);

    void deleteByRoleId(Long roleId);
}
