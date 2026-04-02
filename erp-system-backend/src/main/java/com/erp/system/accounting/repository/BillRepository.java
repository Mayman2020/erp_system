package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.Bill;
import com.erp.system.common.enums.BillStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    @EntityGraph(attributePaths = {"lines", "lines.account", "payableAccount", "taxAccount", "journalEntry", "cancellationJournalEntry"})
    List<Bill> findAllByOrderByBillDateDescIdDesc();

    @EntityGraph(attributePaths = {"lines", "lines.account", "payableAccount", "taxAccount", "journalEntry", "cancellationJournalEntry"})
    Optional<Bill> findById(Long id);

    List<Bill> findByStatusOrderByBillDateDescIdDesc(BillStatus status);

    boolean existsByBillNumberIgnoreCase(String billNumber);
}
