package com.erp.system.pmo.repository;

import com.erp.system.pmo.domain.PmoMilestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PmoMilestoneRepository extends JpaRepository<PmoMilestone, Long> {
    List<PmoMilestone> findByProjectIdOrderBySortOrderAscIdAsc(Long projectId);
}
