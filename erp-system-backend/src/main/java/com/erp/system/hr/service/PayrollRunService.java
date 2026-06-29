package com.erp.system.hr.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryRepository;
import com.erp.system.accounting.service.AccountingPostingService;
import com.erp.system.accounting.support.JournalPostingNarratives;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.domain.PayrollLine;
import com.erp.system.hr.domain.PayrollRun;
import com.erp.system.hr.dto.display.PayrollLineDisplayDto;
import com.erp.system.hr.dto.display.PayrollRunDisplayDto;
import com.erp.system.hr.dto.form.PayrollRunFormDto;
import com.erp.system.hr.repository.PayrollLineRepository;
import com.erp.system.hr.repository.PayrollRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollRunService {

    private static final String MODULE = "HR";

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollLineRepository payrollLineRepository;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;
    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AccountingPostingService accountingPostingService;

    @Transactional(readOnly = true)
    public List<PayrollRunDisplayDto> getAll() {
        return payrollRunRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PayrollRunDisplayDto getById(Long id) {
        return toDisplay(loadPayrollRun(id));
    }

    @Transactional
    public PayrollRunDisplayDto create(PayrollRunFormDto request) {
        PayrollRun payrollRun = new PayrollRun();
        applyForm(payrollRun, request);
        payrollRun.setPayrollNumber(resolveNumber(request.getPayrollNumber()));
        payrollRun.setStatus(TransactionStatus.DRAFT);
        payrollRun = payrollRunRepository.save(payrollRun);
        activityLogService.log(MODULE, "CREATE", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                "Created payroll run " + payrollRun.getPayrollNumber());
        return toDisplay(payrollRun);
    }

    @Transactional
    public PayrollRunDisplayDto update(Long id, PayrollRunFormDto request) {
        PayrollRun payrollRun = loadPayrollRun(id);
        if (payrollRun.getStatus() == TransactionStatus.APPROVED) {
            throw new BusinessException("Approved payroll run cannot be edited");
        }
        applyForm(payrollRun, request);
        payrollRun = payrollRunRepository.save(payrollRun);
        activityLogService.log(MODULE, "UPDATE", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                "Updated payroll run " + payrollRun.getPayrollNumber());
        return toDisplay(payrollRun);
    }

    @Transactional
    public PayrollRunDisplayDto approve(Long id, String actor) {
        PayrollRun payrollRun = loadPayrollRun(id);
        if (payrollRun.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled payroll run cannot be approved");
        }
        if (payrollRun.getStatus() == TransactionStatus.APPROVED) {
            return toDisplay(payrollRun);
        }

        List<PayrollLine> lines = payrollLineRepository.findByPayrollIdOrderByIdAsc(payrollRun.getId());
        BigDecimal payrollAmount = resolvePayrollAmount(payrollRun, lines);
        if (payrollAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payroll amount must be greater than zero");
        }

        Account expenseAccount = accountRepository.findByCode("5110")
                .orElseThrow(() -> new BusinessException("Payroll expense account 5110 not found"));
        Account cashAccount = resolveCashBankAccount();
        String narrative = JournalPostingNarratives.entryHeader(
                payrollRun.getNotes(),
                "Payroll",
                payrollRun.getPayrollNumber()
        );

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                payrollRun.getPeriodEnd(),
                narrative,
                "PAYROLL",
                payrollRun.getId(),
                actor,
                List.of(
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(expenseAccount.getId())
                                .description(JournalPostingNarratives.lineWithAccount(narrative, expenseAccount, true))
                                .debit(payrollAmount)
                                .credit(BigDecimal.ZERO)
                                .build(),
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(cashAccount.getId())
                                .description(JournalPostingNarratives.lineWithAccount(narrative, cashAccount, false))
                                .debit(BigDecimal.ZERO)
                                .credit(payrollAmount)
                                .build()
                )
        );

        payrollRun.setTotalAmount(payrollAmount);
        payrollRun.setJournalEntryId(journalEntry.getId());
        payrollRun.setStatus(TransactionStatus.APPROVED);
        payrollRun = payrollRunRepository.save(payrollRun);
        activityLogService.log(MODULE, "APPROVE", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                "Approved payroll run " + payrollRun.getPayrollNumber());
        return toDisplay(payrollRun);
    }

    @Transactional
    public PayrollRunDisplayDto cancel(Long id, String actor, String reason) {
        PayrollRun payrollRun = loadPayrollRun(id);
        if (payrollRun.getStatus() == TransactionStatus.CANCELLED) {
            return toDisplay(payrollRun);
        }
        if (payrollRun.getJournalEntryId() != null) {
            final Long journalEntryId = payrollRun.getJournalEntryId();
            JournalEntry original = journalEntryRepository.findById(journalEntryId)
                    .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", journalEntryId));
            accountingPostingService.reverseJournal(original, actor, reason, LocalDate.now());
        }
        payrollRun.setStatus(TransactionStatus.CANCELLED);
        payrollRun = payrollRunRepository.save(payrollRun);
        activityLogService.log(MODULE, "CANCEL", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                "Cancelled payroll run " + payrollRun.getPayrollNumber());
        return toDisplay(payrollRun);
    }

    @Transactional
    public void delete(Long id) {
        PayrollRun payrollRun = loadPayrollRun(id);
        if (payrollRun.getStatus() == TransactionStatus.APPROVED) {
            throw new BusinessException("Approved payroll run cannot be deleted");
        }
        payrollRunRepository.delete(payrollRun);
        activityLogService.log(MODULE, "DELETE", "PayrollRun", id, payrollRun.getPayrollNumber(),
                "Deleted payroll run " + payrollRun.getPayrollNumber());
    }

    private String resolveNumber(String requested) {
        String normalized = requested == null ? null : requested.trim();
        if (normalized != null && !normalized.isEmpty()) {
            if (payrollRunRepository.existsByPayrollNumberIgnoreCase(normalized)) {
                throw new BusinessException("Payroll number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("PAYROLL_RUN");
        } catch (Exception exception) {
            return "PAY-" + System.currentTimeMillis();
        }
    }

    private void applyForm(PayrollRun payrollRun, PayrollRunFormDto request) {
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new BusinessException("Payroll period end cannot be before start");
        }
        payrollRun.setPeriodStart(request.getPeriodStart());
        payrollRun.setPeriodEnd(request.getPeriodEnd());
        payrollRun.setTotalAmount(request.getTotalAmount());
        payrollRun.setNotes(request.getNotes());
    }

    private PayrollRun loadPayrollRun(Long id) {
        return payrollRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", id));
    }

    private Account resolveCashBankAccount() {
        for (String code : List.of("1010", "1020", "1110")) {
            var account = accountRepository.findByCode(code);
            if (account.isPresent()) {
                return account.get();
            }
        }
        throw new BusinessException("Cash/bank account not found (tried 1010, 1020, 1110)");
    }

    private BigDecimal resolvePayrollAmount(PayrollRun payrollRun, List<PayrollLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return payrollRun.getTotalAmount() == null ? BigDecimal.ZERO : payrollRun.getTotalAmount();
        }
        BigDecimal lineTotal = lines.stream()
                .map(line -> line.getNetSalary() == null ? BigDecimal.ZERO : line.getNetSalary())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (payrollRun.getTotalAmount() != null
                && payrollRun.getTotalAmount().compareTo(BigDecimal.ZERO) > 0
                && lineTotal.compareTo(BigDecimal.ZERO) > 0
                && payrollRun.getTotalAmount().compareTo(lineTotal) != 0) {
            throw new BusinessException("Payroll header total does not match sum of payroll lines");
        }
        return lineTotal.compareTo(BigDecimal.ZERO) > 0 ? lineTotal : payrollRun.getTotalAmount();
    }

    private PayrollRunDisplayDto toDisplay(PayrollRun payrollRun) {
        List<PayrollLineDisplayDto> lines = payrollLineRepository.findByPayrollIdOrderByIdAsc(payrollRun.getId()).stream()
                .map(line -> PayrollLineDisplayDto.builder()
                        .id(line.getId())
                        .payrollId(line.getPayrollId())
                        .employeeId(line.getEmployeeId())
                        .basicSalary(line.getBasicSalary())
                        .allowances(line.getAllowances())
                        .deductions(line.getDeductions())
                        .netSalary(line.getNetSalary())
                        .createdAt(line.getCreatedAt())
                        .updatedAt(line.getUpdatedAt())
                        .build())
                .toList();
        return PayrollRunDisplayDto.builder()
                .id(payrollRun.getId())
                .payrollNumber(payrollRun.getPayrollNumber())
                .periodStart(payrollRun.getPeriodStart())
                .periodEnd(payrollRun.getPeriodEnd())
                .status(payrollRun.getStatus())
                .totalAmount(payrollRun.getTotalAmount())
                .notes(payrollRun.getNotes())
                .journalEntryId(payrollRun.getJournalEntryId())
                .lines(lines)
                .createdAt(payrollRun.getCreatedAt())
                .updatedAt(payrollRun.getUpdatedAt())
                .build();
    }
}
