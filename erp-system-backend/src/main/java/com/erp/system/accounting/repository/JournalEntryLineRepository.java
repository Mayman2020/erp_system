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
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumDebitBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(line.credit), 0)
            from JournalEntryLine line
            where line.journalEntry.entryDate between :from and :to
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumCreditBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(line.debit), 0)
            from JournalEntryLine line
            where line.account.accountType = :accountType
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumDebitByAccountType(@Param("accountType") AccountingType accountType);

    @Query("""
            select coalesce(sum(line.credit), 0)
            from JournalEntryLine line
            where line.account.accountType = :accountType
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumCreditByAccountType(@Param("accountType") AccountingType accountType);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.id = :accountId
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumNetMovementByAccountId(@Param("accountId") Long accountId);

    @Query("""
            select line
            from JournalEntryLine line
            where line.account.id = :accountId
              and line.journalEntry.entryDate >= coalesce(:fromDate, line.journalEntry.entryDate)
              and line.journalEntry.entryDate <= coalesce(:toDate, line.journalEntry.entryDate)
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
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
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumNetMovementBefore(@Param("accountId") Long accountId, @Param("beforeDate") LocalDate beforeDate);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.id = :accountId
              and line.journalEntry.entryDate between :fromDate and :toDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumNetMovementBetween(@Param("accountId") Long accountId,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate);

    @Query("""
            select new com.erp.system.accounting.dto.display.ProfitLossLineDto(
                a.id, a.code, a.nameEn, a.nameAr,
                coalesce(sum(line.credit - line.debit), 0))
            from JournalEntryLine line
            join line.account a
            where a.accountType = com.erp.system.common.enums.AccountingType.INCOME
              and line.journalEntry.entryDate >= :fromDate
              and line.journalEntry.entryDate <= :toDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            group by a.id, a.code, a.nameEn, a.nameAr
            order by a.code
            """)
    List<com.erp.system.accounting.dto.display.ProfitLossLineDto> aggregateRevenues(
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("""
            select new com.erp.system.accounting.dto.display.ProfitLossLineDto(
                a.id, a.code, a.nameEn, a.nameAr,
                coalesce(sum(line.debit - line.credit), 0))
            from JournalEntryLine line
            join line.account a
            where a.accountType = com.erp.system.common.enums.AccountingType.EXPENSE
              and line.journalEntry.entryDate >= :fromDate
              and line.journalEntry.entryDate <= :toDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            group by a.id, a.code, a.nameEn, a.nameAr
            order by a.code
            """)
    List<com.erp.system.accounting.dto.display.ProfitLossLineDto> aggregateExpenses(
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    @Query("""
            select new com.erp.system.accounting.dto.display.BalanceSheetLineDto(
                a.id, a.code, a.nameEn, a.nameAr,
                coalesce(sum(line.debit - line.credit), 0))
            from JournalEntryLine line
            join line.account a
            where a.accountType = :accountType
              and line.journalEntry.entryDate <= :asOfDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            group by a.id, a.code, a.nameEn, a.nameAr
            order by a.code
            """)
    List<com.erp.system.accounting.dto.display.BalanceSheetLineDto> aggregateBalanceByType(
            @Param("accountType") com.erp.system.common.enums.AccountingType accountType,
            @Param("asOfDate") LocalDate asOfDate);

    @Query("""
            select coalesce(sum(line.credit - line.debit), 0)
            from JournalEntryLine line
            where line.account.accountType = com.erp.system.common.enums.AccountingType.INCOME
              and line.journalEntry.entryDate <= :asOfDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            """)
    BigDecimal sumNetIncomeUpTo(@Param("asOfDate") LocalDate asOfDate);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.accountType = com.erp.system.common.enums.AccountingType.EXPENSE
              and line.journalEntry.entryDate <= :asOfDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            """)
    BigDecimal sumNetExpenseUpTo(@Param("asOfDate") LocalDate asOfDate);

    @Query("""
            select new com.erp.system.accounting.dto.display.ProfitLossLineDto(
                a.id, a.code, a.nameEn, a.nameAr,
                coalesce(sum(line.credit - line.debit), 0))
            from JournalEntryLine line
            join line.account a
            where a.accountType = com.erp.system.common.enums.AccountingType.INCOME
              and line.journalEntry.entryDate >= :fromDate
              and line.journalEntry.entryDate <= :toDate
              and line.journalEntry.currencyCode = :currency
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            group by a.id, a.code, a.nameEn, a.nameAr
            order by a.code
            """)
    List<com.erp.system.accounting.dto.display.ProfitLossLineDto> aggregateRevenuesByCurrency(
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("currency") String currency);

    @Query("""
            select new com.erp.system.accounting.dto.display.ProfitLossLineDto(
                a.id, a.code, a.nameEn, a.nameAr,
                coalesce(sum(line.debit - line.credit), 0))
            from JournalEntryLine line
            join line.account a
            where a.accountType = com.erp.system.common.enums.AccountingType.EXPENSE
              and line.journalEntry.entryDate >= :fromDate
              and line.journalEntry.entryDate <= :toDate
              and line.journalEntry.currencyCode = :currency
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            group by a.id, a.code, a.nameEn, a.nameAr
            order by a.code
            """)
    List<com.erp.system.accounting.dto.display.ProfitLossLineDto> aggregateExpensesByCurrency(
            @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
            @Param("currency") String currency);

    @Query("""
            select new com.erp.system.accounting.dto.display.BalanceSheetLineDto(
                a.id, a.code, a.nameEn, a.nameAr,
                coalesce(sum(line.debit - line.credit), 0))
            from JournalEntryLine line
            join line.account a
            where a.accountType = :accountType
              and line.journalEntry.entryDate <= :asOfDate
              and line.journalEntry.currencyCode = :currency
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.POSTED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            group by a.id, a.code, a.nameEn, a.nameAr
            order by a.code
            """)
    List<com.erp.system.accounting.dto.display.BalanceSheetLineDto> aggregateBalanceByTypeAndCurrency(
            @Param("accountType") com.erp.system.common.enums.AccountingType accountType,
            @Param("asOfDate") LocalDate asOfDate,
            @Param("currency") String currency);

    @Query("select distinct je.currencyCode from JournalEntry je where je.currencyCode is not null")
    List<String> findDistinctCurrencyCodes();

    boolean existsByAccountId(Long accountId);
}
