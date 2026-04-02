package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.Budget;
import com.erp.system.accounting.dto.display.BudgetDisplayDto;
import com.erp.system.accounting.dto.form.BudgetFormDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.BudgetRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.common.enums.BudgetStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    @Transactional(readOnly = true)
    public List<BudgetDisplayDto> getBudgets(BudgetStatus status) {
        List<Budget> budgets = status == null
                ? budgetRepository.findAllByOrderByBudgetYearDescBudgetMonthAscIdDesc()
                : budgetRepository.findByStatusOrderByBudgetYearDescBudgetMonthAscIdDesc(status);
        return budgets.stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public BudgetDisplayDto getBudget(Long id) {
        return toDisplay(loadBudget(id));
    }

    @Transactional
    public BudgetDisplayDto createBudget(BudgetFormDto request) {
        Budget budget = new Budget();
        applyForm(budget, request);
        return toDisplay(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetDisplayDto updateBudget(Long id, BudgetFormDto request) {
        Budget budget = loadBudget(id);
        if (budget.getStatus() == BudgetStatus.CLOSED) {
            throw new BusinessException("Closed budgets cannot be edited");
        }
        applyForm(budget, request);
        return toDisplay(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetDisplayDto changeStatus(Long id, BudgetStatus status) {
        Budget budget = loadBudget(id);
        budget.setStatus(status);
        return toDisplay(budgetRepository.save(budget));
    }

    private void applyForm(Budget budget, BudgetFormDto request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.getAccountId()));
        if (!account.isActive()) {
            throw new BusinessException("Budget account must be active");
        }
        budget.setAccount(account);
        budget.setBudgetName(request.getBudgetName());
        budget.setBudgetYear(request.getBudgetYear());
        budget.setBudgetMonth(request.getBudgetMonth());
        budget.setPlannedAmount(request.getPlannedAmount().setScale(2, RoundingMode.HALF_UP));
        budget.setStatus(request.getStatus());
        budget.setNotes(request.getNotes());
        budget.setActualAmount(calculateActualAmount(account.getId(), request.getBudgetYear(), request.getBudgetMonth()));
    }

    private BigDecimal calculateActualAmount(Long accountId, Integer budgetYear, Integer budgetMonth) {
        LocalDate fromDate;
        LocalDate toDate;
        if (budgetMonth == null) {
            fromDate = LocalDate.of(budgetYear, 1, 1);
            toDate = LocalDate.of(budgetYear, 12, 31);
        } else {
            YearMonth yearMonth = YearMonth.of(budgetYear, budgetMonth);
            fromDate = yearMonth.atDay(1);
            toDate = yearMonth.atEndOfMonth();
        }
        return journalEntryLineRepository.sumNetMovementBetween(accountId, fromDate, toDate).setScale(2, RoundingMode.HALF_UP);
    }

    private Budget loadBudget(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
    }

    private BudgetDisplayDto toDisplay(Budget budget) {
        BigDecimal variance = budget.getPlannedAmount().subtract(budget.getActualAmount()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal variancePercentage = budget.getPlannedAmount().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : variance.divide(budget.getPlannedAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        return BudgetDisplayDto.builder()
                .id(budget.getId())
                .accountId(budget.getAccount().getId())
                .accountCode(budget.getAccount().getCode())
                .accountName(budget.getAccount().getNameEn())
                .budgetName(budget.getBudgetName())
                .budgetYear(budget.getBudgetYear())
                .budgetMonth(budget.getBudgetMonth())
                .plannedAmount(budget.getPlannedAmount())
                .actualAmount(budget.getActualAmount())
                .variance(variance)
                .variancePercentage(variancePercentage)
                .overBudget(budget.getActualAmount().compareTo(budget.getPlannedAmount()) > 0)
                .status(budget.getStatus())
                .notes(budget.getNotes())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
