package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.AccountingCheck;
import com.erp.system.common.enums.CheckStatus;
import com.erp.system.common.enums.CheckType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountingCheckRepository extends JpaRepository<AccountingCheck, Long> {

    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.linkedAccount", "holdingAccount", "journalEntry", "reversalJournalEntry"})
    List<AccountingCheck> findAllByOrderByIssueDateDescIdDesc();

    @EntityGraph(attributePaths = {"bankAccount", "bankAccount.linkedAccount", "holdingAccount", "journalEntry", "reversalJournalEntry"})
    Optional<AccountingCheck> findById(Long id);

    List<AccountingCheck> findByStatusOrderByIssueDateDescIdDesc(CheckStatus status);

    List<AccountingCheck> findByCheckTypeOrderByIssueDateDescIdDesc(CheckType checkType);

    boolean existsByCheckNumberIgnoreCase(String checkNumber);
}
