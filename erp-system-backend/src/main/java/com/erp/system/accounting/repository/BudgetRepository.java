package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.Budget;
import com.erp.system.common.enums.BudgetStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @EntityGraph(attributePaths = {"account"})
    List<Budget> findAllByOrderByBudgetYearDescBudgetMonthAscIdDesc();

    @EntityGraph(attributePaths = {"account"})
    Optional<Budget> findById(Long id);

    List<Budget> findByStatusOrderByBudgetYearDescBudgetMonthAscIdDesc(BudgetStatus status);
}
