package com.erp.system.pmo.repository;

import com.erp.system.pmo.domain.PmoRisk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PmoRiskRepository extends JpaRepository<PmoRisk, Long> {
    List<PmoRisk> findByProjectIdOrderByIdDesc(Long projectId);
}
