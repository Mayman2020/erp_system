package com.erp.system.common.repository;

import com.erp.system.common.entity.LookupValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LookupValueRepository extends JpaRepository<LookupValue, Long> {

    List<LookupValue> findByTypeCodeIgnoreCaseAndActiveTrueOrderBySortOrderAscCodeAsc(String typeCode);
}
