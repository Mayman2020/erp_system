package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.common.enums.JournalEntryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    @EntityGraph(attributePaths = {"lines", "lines.account"})
    List<JournalEntry> findAllByOrderByEntryDateDescIdDesc();

    @EntityGraph(attributePaths = {"lines", "lines.account"})
    Optional<JournalEntry> findById(Long id);

    @EntityGraph(attributePaths = {"lines", "lines.account"})
    Page<JournalEntry> findAllByOrderByEntryDateDescIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"lines", "lines.account"})
    List<JournalEntry> findByStatusOrderByEntryDateDescIdDesc(JournalEntryStatus status);

    @EntityGraph(attributePaths = {"lines", "lines.account"})
    List<JournalEntry> findByEntryDateBetweenOrderByEntryDateDescIdDesc(LocalDate fromDate, LocalDate toDate);

    @EntityGraph(attributePaths = {"lines", "lines.account"})
    @Query("""
           select distinct je
           from JournalEntry je
           left join je.lines line
           where je.status = coalesce(:status, je.status)
             and je.entryDate >= coalesce(:fromDate, je.entryDate)
             and je.entryDate <= coalesce(:toDate, je.entryDate)
             and line.account.id = coalesce(:accountId, line.account.id)
           order by je.entryDate desc, je.id desc
           """)
    List<JournalEntry> searchJournalEntries(@Param("status") JournalEntryStatus status,
                                            @Param("fromDate") LocalDate fromDate,
                                            @Param("toDate") LocalDate toDate,
                                            @Param("accountId") Long accountId);

    boolean existsByReferenceNumberIgnoreCase(String referenceNumber);

    long countByEntryDateBetween(LocalDate fromDate, LocalDate toDate);

    long countByStatus(JournalEntryStatus status);
}
