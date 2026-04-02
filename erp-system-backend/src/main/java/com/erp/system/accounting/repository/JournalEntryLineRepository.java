package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.common.enums.AccountingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    @Query("""
            select coalesce(sum(line.debit), 0)
            from JournalEntryLine line
            where line.journalEntry.entryDate between :from and :to
            """)
    BigDecimal sumDebitBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(line.credit), 0)
            from JournalEntryLine line
            where line.journalEntry.entryDate between :from and :to
            """)
    BigDecimal sumCreditBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(line.debit), 0)
            from JournalEntryLine line
            where line.account.accountType = :accountType
              and line.journalEntry.status in (com.erp.system.common.enums.JournalEntryStatus.POSTED,
                                               com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            """)
    BigDecimal sumDebitByAccountType(@Param("accountType") AccountingType accountType);

    @Query("""
            select coalesce(sum(line.credit), 0)
            from JournalEntryLine line
            where line.account.accountType = :accountType
              and line.journalEntry.status in (com.erp.system.common.enums.JournalEntryStatus.POSTED,
                                               com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            """)
    BigDecimal sumCreditByAccountType(@Param("accountType") AccountingType accountType);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.id = :accountId
              and line.journalEntry.status in (com.erp.system.common.enums.JournalEntryStatus.POSTED,
                                               com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            """)
    BigDecimal sumNetMovementByAccountId(@Param("accountId") Long accountId);

    @Query("""
            select line
            from JournalEntryLine line
            where line.account.id = :accountId
              and (:fromDate is null or line.journalEntry.entryDate >= :fromDate)
              and (:toDate is null or line.journalEntry.entryDate <= :toDate)
              and line.journalEntry.status in (com.erp.system.common.enums.JournalEntryStatus.POSTED,
                                               com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            order by line.journalEntry.entryDate asc, line.journalEntry.id asc, line.lineNumber asc
            """)
    List<JournalEntryLine> findLedgerLines(@Param("accountId") Long accountId,
                                           @Param("fromDate") LocalDate fromDate,
                                           @Param("toDate") LocalDate toDate);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.id = :accountId
              and line.journalEntry.entryDate < :beforeDate
              and line.journalEntry.status in (com.erp.system.common.enums.JournalEntryStatus.POSTED,
                                               com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            """)
    BigDecimal sumNetMovementBefore(@Param("accountId") Long accountId, @Param("beforeDate") LocalDate beforeDate);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.id = :accountId
              and line.journalEntry.entryDate between :fromDate and :toDate
              and line.journalEntry.status in (com.erp.system.common.enums.JournalEntryStatus.POSTED,
                                               com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            """)
    BigDecimal sumNetMovementBetween(@Param("accountId") Long accountId,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate);

    boolean existsByAccountId(Long accountId);
}
