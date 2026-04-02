package com.erp.system.auth.repository;

import com.erp.system.auth.domain.AccessRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccessRoleRepository extends JpaRepository<AccessRole, Long> {

    List<AccessRole> findAllByOrderBySystemRoleDescCodeAsc();

    List<AccessRole> findByIdIn(Collection<Long> ids);

    List<AccessRole> findByCodeIn(Collection<String> codes);

    Optional<AccessRole> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
