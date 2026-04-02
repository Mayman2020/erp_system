package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.AccountingTransaction;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.enums.TransactionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountingTransactionRepository extends JpaRepository<AccountingTransaction, Long> {

    @EntityGraph(attributePaths = {"debitAccount", "creditAccount", "journalEntry", "originalTransaction"})
    List<AccountingTransaction> findAllByOrderByTransactionDateDescIdDesc();

    @EntityGraph(attributePaths = {"debitAccount", "creditAccount", "journalEntry", "originalTransaction"})
    Optional<AccountingTransaction> findById(Long id);

    List<AccountingTransaction> findByTransactionTypeOrderByTransactionDateDescIdDesc(TransactionType transactionType);

    List<AccountingTransaction> findByStatusOrderByTransactionDateDescIdDesc(TransactionStatus status);

    boolean existsByReferenceIgnoreCase(String reference);
}
