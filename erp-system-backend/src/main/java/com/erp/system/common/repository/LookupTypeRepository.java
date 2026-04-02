package com.erp.system.common.repository;

import com.erp.system.common.entity.LookupType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LookupTypeRepository extends JpaRepository<LookupType, Long> {

    List<LookupType> findAllByOrderBySortOrderAscCodeAsc();

    Optional<LookupType> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
