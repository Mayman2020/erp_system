package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.accounting.dto.display.AccountTypeMovementDto;
import com.erp.system.accounting.dto.display.DailyPostedMovementDto;
import com.erp.system.accounting.dto.display.MonthlyPostedRollupDto;
import com.erp.system.common.enums.AccountingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    @Query("""
            select coalesce(sum(line.debit), 0)
            from JournalEntryLine line
            where line.journalEntry.entryDate between :from and :to
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumDebitBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(line.credit), 0)
            from JournalEntryLine line
            where line.journalEntry.entryDate between :from and :to
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumCreditBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(line.debit), 0)
            from JournalEntryLine line
            where line.account.accountType = :accountType
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumDebitByAccountType(@Param("accountType") AccountingType accountType);

    @Query("""
            select coalesce(sum(line.credit), 0)
            from JournalEntryLine line
            where line.account.accountType = :accountType
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumCreditByAccountType(@Param("accountType") AccountingType accountType);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.id = :accountId
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
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
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            order by line.journalEntry.entryDate asc, line.journalEntry.id asc, line.lineNumber asc
            """)
    List<JournalEntryLine> findLedgerLines(@Param("accountId") Long accountId,
                                           @Param("fromDate") LocalDate fromDate,
                                           @Param("toDate") LocalDate toDate);

    @Query("""
            select line
            from JournalEntryLine line
            where line.account.id in :accountIds
              and line.journalEntry.entryDate >= coalesce(:fromDate, line.journalEntry.entryDate)
              and line.journalEntry.entryDate <= coalesce(:toDate, line.journalEntry.entryDate)
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            order by line.journalEntry.entryDate asc, line.journalEntry.id asc, line.lineNumber asc
            """)
    List<JournalEntryLine> findLedgerLinesForAccountIds(@Param("accountIds") Collection<Long> accountIds,
                                                      @Param("fromDate") LocalDate fromDate,
                                                      @Param("toDate") LocalDate toDate);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.id = :accountId
              and line.journalEntry.entryDate < :beforeDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumNetMovementBefore(@Param("accountId") Long accountId, @Param("beforeDate") LocalDate beforeDate);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.id in :accountIds
              and line.journalEntry.entryDate < :beforeDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED
              )
            """)
    BigDecimal sumNetMovementBeforeAccountIds(@Param("accountIds") Collection<Long> accountIds,
                                              @Param("beforeDate") LocalDate beforeDate);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.id = :accountId
              and line.journalEntry.entryDate between :fromDate and :toDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
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
            where a.accountType = com.erp.system.common.enums.AccountingType.REVENUE
              and line.journalEntry.entryDate >= :fromDate
              and line.journalEntry.entryDate <= :toDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
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
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
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
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
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
            where line.account.accountType = com.erp.system.common.enums.AccountingType.REVENUE
              and line.journalEntry.entryDate <= :asOfDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            """)
    BigDecimal sumNetIncomeUpTo(@Param("asOfDate") LocalDate asOfDate);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.accountType = com.erp.system.common.enums.AccountingType.EXPENSE
              and line.journalEntry.entryDate <= :asOfDate
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
                    com.erp.system.common.enums.JournalEntryStatus.REVERSED)
            """)
    BigDecimal sumNetExpenseUpTo(@Param("asOfDate") LocalDate asOfDate);

    @Query("""
            select new com.erp.system.accounting.dto.display.ProfitLossLineDto(
                a.id, a.code, a.nameEn, a.nameAr,
                coalesce(sum(line.credit - line.debit), 0))
            from JournalEntryLine line
            join line.account a
            where a.accountType = com.erp.system.common.enums.AccountingType.REVENUE
              and line.journalEntry.entryDate >= :fromDate
              and line.journalEntry.entryDate <= :toDate
              and line.journalEntry.currencyCode = :currency
              and line.journalEntry.status in (
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
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
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
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
                    com.erp.system.common.enums.JournalEntryStatus.APPROVED,
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

    @Query("""
            select coalesce(sum(line.debit), 0)
            from JournalEntryLine line
            where line.journalEntry.entryDate between :from and :to
              and line.journalEntry.status = com.erp.system.common.enums.JournalEntryStatus.APPROVED
            """)
    BigDecimal sumPostedDebitBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(line.credit), 0)
            from JournalEntryLine line
            where line.journalEntry.entryDate between :from and :to
              and line.journalEntry.status = com.erp.system.common.enums.JournalEntryStatus.APPROVED
            """)
    BigDecimal sumPostedCreditBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select new com.erp.system.accounting.dto.display.AccountTypeMovementDto(
                line.account.accountType,
                coalesce(sum(line.debit), 0),
                coalesce(sum(line.credit), 0)
            )
            from JournalEntryLine line
            where line.journalEntry.status = com.erp.system.common.enums.JournalEntryStatus.APPROVED
            group by line.account.accountType
            """)
    List<AccountTypeMovementDto> aggregatePostedMovementByAccountType();

    @Query("""
            select new com.erp.system.accounting.dto.display.AccountTypeMovementDto(
                line.account.accountType,
                coalesce(sum(line.debit), 0),
                coalesce(sum(line.credit), 0)
            )
            from JournalEntryLine line
            where line.journalEntry.status = com.erp.system.common.enums.JournalEntryStatus.APPROVED
              and line.journalEntry.entryDate between :from and :to
            group by line.account.accountType
            """)
    List<AccountTypeMovementDto> aggregatePostedMovementByAccountTypeBetween(
            @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(line.credit - line.debit), 0)
            from JournalEntryLine line
            where line.account.accountType = com.erp.system.common.enums.AccountingType.REVENUE
              and line.journalEntry.entryDate between :from and :to
              and line.journalEntry.status = com.erp.system.common.enums.JournalEntryStatus.APPROVED
            """)
    BigDecimal sumPostedRevenueBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.account.accountType = com.erp.system.common.enums.AccountingType.EXPENSE
              and line.journalEntry.entryDate between :from and :to
              and line.journalEntry.status = com.erp.system.common.enums.JournalEntryStatus.APPROVED
            """)
    BigDecimal sumPostedExpenseBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select new com.erp.system.accounting.dto.display.DailyPostedMovementDto(
                line.journalEntry.entryDate,
                coalesce(sum(line.debit), 0),
                coalesce(sum(line.credit), 0))
            from JournalEntryLine line
            where line.journalEntry.entryDate between :from and :to
              and line.journalEntry.status = com.erp.system.common.enums.JournalEntryStatus.APPROVED
            group by line.journalEntry.entryDate
            order by line.journalEntry.entryDate
            """)
    List<DailyPostedMovementDto> aggregatePostedDebitsCreditsByDay(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select new com.erp.system.accounting.dto.display.MonthlyPostedRollupDto(
                year(line.journalEntry.entryDate),
                month(line.journalEntry.entryDate),
                coalesce(sum(line.debit), 0),
                coalesce(sum(line.credit), 0),
                coalesce(sum(case when line.account.accountType = com.erp.system.common.enums.AccountingType.REVENUE
                    then line.credit - line.debit else 0 end), 0),
                coalesce(sum(case when line.account.accountType = com.erp.system.common.enums.AccountingType.EXPENSE
                    then line.debit - line.credit else 0 end), 0))
            from JournalEntryLine line
            where line.journalEntry.entryDate between :from and :to
              and line.journalEntry.status = com.erp.system.common.enums.JournalEntryStatus.APPROVED
            group by year(line.journalEntry.entryDate), month(line.journalEntry.entryDate)
            order by year(line.journalEntry.entryDate), month(line.journalEntry.entryDate)
            """)
    List<MonthlyPostedRollupDto> aggregatePostedRollupByMonth(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select line.account.id, coalesce(sum(line.debit - line.credit), 0)
            from JournalEntryLine line
            where line.journalEntry.status = com.erp.system.common.enums.JournalEntryStatus.APPROVED
              and line.account.id in :accountIds
            group by line.account.id
            """)
    List<Object[]> sumPostedNetMovementGroupedByAccountIds(@Param("accountIds") Collection<Long> accountIds);
}
