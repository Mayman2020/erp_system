package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.Reconciliation;
import com.erp.system.common.enums.ReconciliationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReconciliationRepository extends JpaRepository<Reconciliation, Long> {

    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.linkedAccount", "lines"})
    List<Reconciliation> findAllByOrderByStatementEndDateDescIdDesc();

    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.linkedAccount", "lines"})
    Optional<Reconciliation> findById(Long id);

    List<Reconciliation> findByStatusOrderByStatementEndDateDescIdDesc(ReconciliationStatus status);
}
