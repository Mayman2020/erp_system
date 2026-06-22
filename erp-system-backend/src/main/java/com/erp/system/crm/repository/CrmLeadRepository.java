package com.erp.system.crm.repository;

import com.erp.system.crm.domain.CrmLead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmLeadRepository extends JpaRepository<CrmLead, Long> {
    List<CrmLead> findAllByOrderByIdDesc();

}
