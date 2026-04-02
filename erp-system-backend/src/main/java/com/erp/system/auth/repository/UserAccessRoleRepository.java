package com.erp.system.auth.repository;

import com.erp.system.auth.domain.UserAccessRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAccessRoleRepository extends JpaRepository<UserAccessRole, Long> {

    List<UserAccessRole> findByUserIdOrderByRoleCodeAsc(Long userId);

    List<UserAccessRole> findByRoleIdOrderByUserUsernameAsc(Long roleId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);
}
