package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.Bill;
import com.erp.system.common.enums.BillStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    @EntityGraph(attributePaths = {"lines", "lines.account", "payableAccount", "taxAccount", "journalEntry", "cancellationJournalEntry"})
    List<Bill> findAllByOrderByBillDateDescIdDesc();

    @EntityGraph(attributePaths = {"lines", "lines.account", "payableAccount", "taxAccount", "journalEntry", "cancellationJournalEntry"})
    Optional<Bill> findById(Long id);

    List<Bill> findByStatusOrderByBillDateDescIdDesc(BillStatus status);

    boolean existsByBillNumberIgnoreCase(String billNumber);

    @Query("""
            select coalesce(sum(b.outstandingAmount), 0)
            from Bill b
            where b.status <> com.erp.system.common.enums.BillStatus.CANCELLED
            """)
    BigDecimal sumOutstandingAmountExcludingCancelled();

    @Query("""
            select coalesce(sum(b.paidAmount), 0)
            from Bill b
            where b.status <> com.erp.system.common.enums.BillStatus.CANCELLED
            """)
    BigDecimal sumPaidAmountExcludingCancelled();
}
