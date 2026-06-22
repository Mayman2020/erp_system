package com.erp.system.projects.repository;

import com.erp.system.projects.domain.ProjectExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectExpenseRepository extends JpaRepository<ProjectExpense, Long> {
    List<ProjectExpense> findAllByOrderByIdDesc();

}
