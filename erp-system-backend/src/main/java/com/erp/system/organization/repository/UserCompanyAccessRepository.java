package com.erp.system.organization.repository;

import com.erp.system.organization.domain.UserCompanyAccess;
import com.erp.system.organization.domain.UserCompanyAccessId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserCompanyAccessRepository extends JpaRepository<UserCompanyAccess, UserCompanyAccessId> {
    boolean existsByUserIdAndCompanyIdAndCompanyActiveTrue(Long userId, Long companyId);
    List<UserCompanyAccess> findByUserIdAndCompanyActiveTrueOrderByCompanyCodeAsc(Long userId);
    Optional<UserCompanyAccess> findFirstByUserIdAndDefaultCompanyTrueAndCompanyActiveTrue(Long userId);
}
