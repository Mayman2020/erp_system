package com.erp.system.crm.repository;

import com.erp.system.crm.domain.CrmActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmActivityRepository extends JpaRepository<CrmActivity, Long> {
    List<CrmActivity> findAllByOrderByIdDesc();

}
