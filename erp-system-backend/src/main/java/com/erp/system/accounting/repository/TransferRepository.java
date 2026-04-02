package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.Transfer;
import com.erp.system.common.enums.TransferStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    @EntityGraph(attributePaths = {"sourceAccount", "destinationAccount", "journalEntry", "reversalJournalEntry"})
    List<Transfer> findAllByOrderByTransferDateDescIdDesc();

    @EntityGraph(attributePaths = {"sourceAccount", "destinationAccount", "journalEntry", "reversalJournalEntry"})
    Optional<Transfer> findById(Long id);

    List<Transfer> findByStatusOrderByTransferDateDescIdDesc(TransferStatus status);

    boolean existsByReferenceIgnoreCase(String reference);
}
