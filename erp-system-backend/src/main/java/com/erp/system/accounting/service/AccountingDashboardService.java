package com.erp.system.accounting.service;

import com.erp.system.accounting.dto.display.AccountTypeAmountDto;
import com.erp.system.accounting.dto.display.AccountTypeMovementDto;
import com.erp.system.accounting.dto.display.AccountingDashboardDisplayDto;
import com.erp.system.accounting.dto.display.DailyPostedMovementDto;
import com.erp.system.accounting.dto.display.MonthlyPostedRollupDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.BankAccountRepository;
import com.erp.system.accounting.repository.BillRepository;
import com.erp.system.accounting.repository.BudgetRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.accounting.repository.JournalEntryRepository;
import com.erp.system.accounting.repository.PaymentVoucherRepository;
import com.erp.system.accounting.repository.ReceiptVoucherRepository;
import com.erp.system.common.dto.PageResultDto;
import com.erp.system.common.enums.AccountingType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountingDashboardService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final PaymentVoucherRepository paymentVoucherRepository;
    private final ReceiptVoucherRepository receiptVoucherRepository;
    private final BillRepository billRepository;
    private final BudgetRepository budgetRepository;
    private final BankAccountRepository bankAccountRepository;

    @Transactional(readOnly = true)
    public AccountingDashboardDisplayDto getDashboard(LocalDate fromDate, LocalDate toDate) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveTo = toDate != null ? toDate : today;
        Map<AccountingType, BigDecimal> openingBalances = openingBalanceMap();

        Map<AccountingType, AccountTypeMovementDto> balanceSheetMovements;
        Map<AccountingType, AccountTypeMovementDto> incomeMovements;
        if (fromDate != null) {
            balanceSheetMovements = postedMovementMapBetween(LocalDate.of(1900, 1, 1), effectiveTo);
            incomeMovements = postedMovementMapBetween(fromDate, effectiveTo);
        } else {
            Map<AccountingType, AccountTypeMovementDto> allMovements = postedMovementMap();
            balanceSheetMovements = allMovements;
            incomeMovements = allMovements;
        }

        BigDecimal totalRevenue = balanceFor(AccountingType.REVENUE, openingBalances, incomeMovements);
        BigDecimal totalExpenses = balanceFor(AccountingType.EXPENSE, openingBalances, incomeMovements);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal payablesOutstanding = billRepository.sumOutstandingAmountExcludingCancelled();
        BigDecimal payablesPaid = billRepository.sumPaidAmountExcludingCancelled();

        LocalDate weekStart = today.minusDays(6);
        List<DailyPostedMovementDto> dailyPosted =
                journalEntryLineRepository.aggregatePostedDebitsCreditsByDay(weekStart, today);

        YearMonth currentMonth = YearMonth.from(today);
        LocalDate rollStart = currentMonth.minusMonths(11).atDay(1);
        LocalDate rollEnd = currentMonth.atEndOfMonth();
        List<MonthlyPostedRollupDto> monthlyPosted =
                journalEntryLineRepository.aggregatePostedRollupByMonth(rollStart, rollEnd);

        Pageable recentFive = PageRequest.of(0, 5, dashboardDefaultSort("journals"));

        return AccountingDashboardDisplayDto.builder()
                .totalAssets(balanceFor(AccountingType.ASSET, openingBalances, balanceSheetMovements))
                .totalLiabilities(balanceFor(AccountingType.LIABILITY, openingBalances, balanceSheetMovements))
                .totalEquity(balanceFor(AccountingType.EQUITY, openingBalances, balanceSheetMovements))
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .weekDebitSeries(weekSeriesFromDaily(dailyPosted, today, true))
                .weekCreditSeries(weekSeriesFromDaily(dailyPosted, today, false))
                .rollingMonthRevenueSeries(rollingSeries(monthlyPosted, currentMonth, MonthlyPostedRollupDto::revenueTotal))
                .rollingMonthExpenseSeries(rollingSeries(monthlyPosted, currentMonth, MonthlyPostedRollupDto::expenseTotal))
                .rollingMonthNetProfitSeries(rollingNetProfitSeries(monthlyPosted, currentMonth))
                .rollingMonthDebitSeries(rollingSeries(monthlyPosted, currentMonth, MonthlyPostedRollupDto::debitTotal))
                .rollingMonthCreditSeries(rollingSeries(monthlyPosted, currentMonth, MonthlyPostedRollupDto::creditTotal))
                .recentJournals(journalEntryRepository.pageDashboardRecentJournals(recentFive).getContent())
                .recentPayments(
                        paymentVoucherRepository
                                .pageDashboardRecentPayments(
                                        PageRequest.of(0, 5, dashboardDefaultSort("payments")))
                                .getContent())
                .recentReceipts(
                        receiptVoucherRepository
                                .pageDashboardRecentReceipts(
                                        PageRequest.of(0, 5, dashboardDefaultSort("receipts")))
                                .getContent())
                .receivablesSummary(totalAssetReceivables())
                .payablesOutstanding(payablesOutstanding)
                .payablesPaid(payablesPaid)
                .budgetSummaries(
                        budgetRepository.findTop6ByOrderByBudgetYearDescBudgetMonthAscIdDesc().stream()
                                .map(budget -> AccountingDashboardDisplayDto.BudgetSnapshot.builder()
                                        .id(budget.getId())
                                        .label((budget.getBudgetName() == null || budget.getBudgetName().isBlank()
                                                ? budget.getAccount().getCode()
                                                : budget.getBudgetName()))
                                        .planned(budget.getPlannedAmount())
                                        .actual(budget.getActualAmount())
                                        .variance(budget.getPlannedAmount().subtract(budget.getActualAmount()))
                                        .build())
                                .toList())
                .bankBalances(bankBalanceSnapshots())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResultDto<AccountingDashboardDisplayDto.RecentDocument> getRecentActivity(
            String kind,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Sort sort = dashboardSort(kind, sortBy, sortDirection);
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<AccountingDashboardDisplayDto.RecentDocument> resultPage =
                switch ((kind == null ? "" : kind).toLowerCase()) {
                    case "payments" -> paymentVoucherRepository.pageDashboardRecentPayments(pageable);
                    case "receipts" -> receiptVoucherRepository.pageDashboardRecentReceipts(pageable);
                    default -> journalEntryRepository.pageDashboardRecentJournals(pageable);
                };

        int totalPages = resultPage.getTotalPages();
        if (totalPages == 0 && resultPage.getTotalElements() == 0) {
            totalPages = 1;
        }

        return PageResultDto.<AccountingDashboardDisplayDto.RecentDocument>builder()
                .items(resultPage.getContent())
                .page(resultPage.getNumber())
                .size(resultPage.getSize())
                .totalItems(resultPage.getTotalElements())
                .totalPages(totalPages)
                .hasNext(resultPage.hasNext())
                .hasPrevious(resultPage.hasPrevious())
                .build();
    }

    private Sort dashboardDefaultSort(String kind) {
        return dashboardSort(kind, null, null);
    }

    private Sort dashboardSort(String kind, String sortBy, String sortDirection) {
        Sort.Direction direction =
                "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sb = sortBy == null ? "" : sortBy.toLowerCase();
        return switch ((kind == null ? "" : kind).toLowerCase()) {
            case "payments", "receipts" -> switch (sb) {
                case "amount" -> Sort.by(direction, "amount");
                case "reference" -> Sort.by(direction, "reference");
                default -> Sort.by(direction, "voucherDate").and(Sort.by(direction, "id"));
            };
            default -> switch (sb) {
                case "amount" -> Sort.by(direction, "totalDebit");
                case "reference" -> Sort.by(direction, "referenceNumber");
                default -> Sort.by(direction, "entryDate").and(Sort.by(direction, "id"));
            };
        };
    }

    private List<BigDecimal> weekSeriesFromDaily(List<DailyPostedMovementDto> rows, LocalDate today, boolean debit) {
        Map<LocalDate, DailyPostedMovementDto> byDay =
                rows.stream().collect(Collectors.toMap(DailyPostedMovementDto::day, Function.identity()));
        List<BigDecimal> series = new ArrayList<>(7);
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            LocalDate day = today.minusDays(daysAgo);
            DailyPostedMovementDto row = byDay.get(day);
            if (row == null) {
                series.add(ZERO);
            } else {
                series.add(debit ? row.debitTotal() : row.creditTotal());
            }
        }
        return series;
    }

    private List<BigDecimal> rollingSeries(
            List<MonthlyPostedRollupDto> rows,
            YearMonth currentMonth,
            Function<MonthlyPostedRollupDto, BigDecimal> field
    ) {
        Map<YearMonth, MonthlyPostedRollupDto> byMonth = rows.stream()
                .collect(Collectors.toMap(r -> YearMonth.of(r.year(), r.month()), Function.identity()));
        List<BigDecimal> series = new ArrayList<>(12);
        for (int monthsAgo = 11; monthsAgo >= 0; monthsAgo--) {
            YearMonth ym = currentMonth.minusMonths(monthsAgo);
            MonthlyPostedRollupDto row = byMonth.get(ym);
            series.add(row == null ? ZERO : field.apply(row));
        }
        return series;
    }

    private List<BigDecimal> rollingNetProfitSeries(List<MonthlyPostedRollupDto> rows, YearMonth currentMonth) {
        Map<YearMonth, MonthlyPostedRollupDto> byMonth = rows.stream()
                .collect(Collectors.toMap(r -> YearMonth.of(r.year(), r.month()), Function.identity()));
        List<BigDecimal> series = new ArrayList<>(12);
        for (int monthsAgo = 11; monthsAgo >= 0; monthsAgo--) {
            YearMonth ym = currentMonth.minusMonths(monthsAgo);
            MonthlyPostedRollupDto row = byMonth.get(ym);
            if (row == null) {
                series.add(ZERO);
            } else {
                series.add(row.revenueTotal().subtract(row.expenseTotal()));
            }
        }
        return series;
    }

    private BigDecimal balanceFor(AccountingType accountType,
                                  Map<AccountingType, BigDecimal> openingBalances,
                                  Map<AccountingType, AccountTypeMovementDto> postedMovements) {
        BigDecimal openingBalance = switch (accountType) {
            case ASSET, LIABILITY, EQUITY -> openingBalances.getOrDefault(accountType, BigDecimal.ZERO);
            default -> BigDecimal.ZERO;
        };
        AccountTypeMovementDto movementRow = postedMovements.get(accountType);
        BigDecimal debit = movementRow == null || movementRow.debitTotal() == null ? BigDecimal.ZERO : movementRow.debitTotal();
        BigDecimal credit = movementRow == null || movementRow.creditTotal() == null ? BigDecimal.ZERO : movementRow.creditTotal();
        BigDecimal movement = switch (accountType) {
            case ASSET, EXPENSE -> debit.subtract(credit);
            case LIABILITY, EQUITY, REVENUE -> credit.subtract(debit);
        };
        return openingBalance.add(movement);
    }

    /**
     * Matches legacy semantics: for each receipt whose revenue account is ASSET, add (voucher count for that account)
     * times (posted net movement on that account). Implemented via grouped SQL, not per-voucher queries.
     */
    private BigDecimal totalAssetReceivables() {
        List<Object[]> counts = receiptVoucherRepository.countReceiptsGroupedByAssetRevenueAccountId();
        if (counts.isEmpty()) {
            return ZERO;
        }
        List<Long> accountIds = counts.stream().map(r -> (Long) r[0]).toList();
        List<Object[]> netRows = journalEntryLineRepository.sumPostedNetMovementGroupedByAccountIds(accountIds);
        Map<Long, BigDecimal> netByAccountId = netRows.stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (BigDecimal) r[1]));
        BigDecimal sum = ZERO;
        for (Object[] row : counts) {
            Long accountId = (Long) row[0];
            long voucherCount = (Long) row[1];
            BigDecimal net = netByAccountId.getOrDefault(accountId, ZERO);
            sum = sum.add(net.multiply(BigDecimal.valueOf(voucherCount)));
        }
        return sum;
    }

    private Map<AccountingType, BigDecimal> openingBalanceMap() {
        Map<AccountingType, BigDecimal> balances = new EnumMap<>(AccountingType.class);
        for (AccountTypeAmountDto row : accountRepository.aggregateSignedOpeningBalancesByType()) {
            balances.put(row.accountType(), normalizeForDisplay(row.accountType(), row.amount()));
        }
        return balances;
    }

    private Map<AccountingType, AccountTypeMovementDto> postedMovementMap() {
        Map<AccountingType, AccountTypeMovementDto> movements = new EnumMap<>(AccountingType.class);
        for (AccountTypeMovementDto row : journalEntryLineRepository.aggregatePostedMovementByAccountType()) {
            movements.put(row.accountType(), row);
        }
        return movements;
    }

    private Map<AccountingType, AccountTypeMovementDto> postedMovementMapBetween(LocalDate from, LocalDate to) {
        Map<AccountingType, AccountTypeMovementDto> movements = new EnumMap<>(AccountingType.class);
        for (AccountTypeMovementDto row : journalEntryLineRepository.aggregatePostedMovementByAccountTypeBetween(from, to)) {
            movements.put(row.accountType(), row);
        }
        return movements;
    }

    private BigDecimal normalizeForDisplay(AccountingType accountType, BigDecimal signedBalance) {
        return switch (accountType) {
            case ASSET, EXPENSE -> signedBalance;
            case LIABILITY, EQUITY, REVENUE -> signedBalance.negate();
        };
    }

    private List<AccountingDashboardDisplayDto.BankBalance> bankBalanceSnapshots() {
        var accounts = bankAccountRepository.findAllByOrderByBankNameAscAccountNumberAsc();
        if (accounts.isEmpty()) {
            return List.of();
        }
        List<Long> linkedIds = accounts.stream()
                .map(a -> a.getLinkedAccount().getId())
                .distinct()
                .toList();
        List<Object[]> netRows = journalEntryLineRepository.sumPostedNetMovementGroupedByAccountIds(linkedIds);
        Map<Long, BigDecimal> netByAccountId = netRows.stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (BigDecimal) r[1]));
        return accounts.stream()
                .map(account -> {
                    BigDecimal opening = account.getOpeningBalance() == null ? ZERO : account.getOpeningBalance();
                    BigDecimal net = netByAccountId.getOrDefault(account.getLinkedAccount().getId(), ZERO);
                    return AccountingDashboardDisplayDto.BankBalance.builder()
                            .id(account.getId())
                            .bankName(account.getBankName())
                            .accountNumber(account.getAccountNumber())
                            .balance(opening.add(net))
                            .currency(account.getCurrency())
                            .build();
                })
                .toList();
    }
}
