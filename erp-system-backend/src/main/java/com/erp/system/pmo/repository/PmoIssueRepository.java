package com.erp.system.pmo.repository;

import com.erp.system.pmo.domain.PmoIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PmoIssueRepository extends JpaRepository<PmoIssue, Long> {
    List<PmoIssue> findByProjectIdOrderByIdDesc(Long projectId);
}
