package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.ReconciliationLine;
import com.erp.system.common.enums.ReconciliationLineSourceType;
import com.erp.system.common.enums.ReconciliationLineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReconciliationLineRepository extends JpaRepository<ReconciliationLine, Long> {

    List<ReconciliationLine> findByReconciliationIdOrderByTransactionDateAscIdAsc(Long reconciliationId);

    List<ReconciliationLine> findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(Long reconciliationId,
                                                                                                      ReconciliationLineSourceType transactionType);

    long countByReconciliationIdAndStatus(Long reconciliationId, ReconciliationLineStatus status);
}
