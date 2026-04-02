package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.BankAccount;
import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.accounting.domain.Reconciliation;
import com.erp.system.accounting.domain.ReconciliationLine;
import com.erp.system.accounting.dto.display.ReconciliationDisplayDto;
import com.erp.system.accounting.dto.display.ReconciliationBankAccountDto;
import com.erp.system.accounting.dto.display.ReconciliationLineDisplayDto;
import com.erp.system.accounting.dto.display.ReconciliationSummaryDto;
import com.erp.system.accounting.dto.form.ReconciliationFormDto;
import com.erp.system.accounting.dto.form.ReconciliationLineFormDto;
import com.erp.system.accounting.repository.BankAccountRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.accounting.repository.ReconciliationLineRepository;
import com.erp.system.accounting.repository.ReconciliationRepository;
import com.erp.system.common.enums.ReconciliationLineSourceType;
import com.erp.system.common.enums.ReconciliationLineStatus;
import com.erp.system.common.enums.ReconciliationStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ReconciliationRepository reconciliationRepository;
    private final ReconciliationLineRepository reconciliationLineRepository;
    private final BankAccountRepository bankAccountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    @Transactional(readOnly = true)
    public List<ReconciliationDisplayDto> getReconciliations(ReconciliationStatus status) {
        List<Reconciliation> reconciliations = status == null
                ? reconciliationRepository.findAllByOrderByStatementEndDateDescIdDesc()
                : reconciliationRepository.findByStatusOrderByStatementEndDateDescIdDesc(status);
        return reconciliations.stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public List<ReconciliationBankAccountDto> getReconciliationBankAccounts() {
        return bankAccountRepository.findAllByOrderByBankNameAscAccountNumberAsc().stream()
                .filter(BankAccount::isActive)
                .map(account -> ReconciliationBankAccountDto.builder()
                        .id(account.getId())
                        .bankName(account.getBankName())
                        .accountNumber(account.getAccountNumber())
                        .currency(account.getCurrency())
                        .currentBalance(account.getCurrentBalance())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public ReconciliationDisplayDto getReconciliation(Long id) {
        return toDisplay(loadReconciliation(id));
    }

    @Transactional(readOnly = true)
    public List<ReconciliationLineDisplayDto> getStatementLines(Long reconciliationId) {
        return reconciliationLineRepository.findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        reconciliationId,
                        ReconciliationLineSourceType.BANK_STATEMENT)
                .stream()
                .map(this::toLineDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReconciliationLineDisplayDto> getSystemTransactions(Long reconciliationId) {
        return reconciliationLineRepository.findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        reconciliationId,
                        ReconciliationLineSourceType.SYSTEM_TRANSACTION)
                .stream()
                .map(this::toLineDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReconciliationSummaryDto getSummary(Long reconciliationId) {
        ReconciliationDisplayDto display = toDisplay(loadReconciliation(reconciliationId));
        return ReconciliationSummaryDto.builder()
                .reconciliationId(display.getId())
                .openingBalance(display.getOpeningBalance())
                .closingBalance(display.getClosingBalance())
                .systemEndingBalance(display.getSystemEndingBalance())
                .difference(display.getDifference())
                .matchedCount(display.getMatchedCount())
                .partiallyMatchedCount(display.getPartiallyMatchedCount())
                .unmatchedCount(display.getUnmatchedCount())
                .build();
    }

    @Transactional
    public ReconciliationDisplayDto createReconciliation(ReconciliationFormDto request) {
        if (request.getStatementEndDate().isBefore(request.getStatementStartDate())) {
            throw new BusinessException("Statement end date cannot be before start date");
        }
        BankAccount bankAccount = bankAccountRepository.findById(request.getBankAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", request.getBankAccountId()));

        BigDecimal systemEndingBalance = bankAccount.getOpeningBalance()
                .add(journalEntryLineRepository.sumNetMovementByAccountId(bankAccount.getLinkedAccount().getId()))
                .setScale(2, RoundingMode.HALF_UP);

        Reconciliation reconciliation = Reconciliation.builder()
                .bankAccount(bankAccount)
                .statementStartDate(request.getStatementStartDate())
                .statementEndDate(request.getStatementEndDate())
                .openingBalance(request.getOpeningBalance().setScale(2, RoundingMode.HALF_UP))
                .closingBalance(request.getClosingBalance().setScale(2, RoundingMode.HALF_UP))
                .systemEndingBalance(systemEndingBalance)
                .difference(request.getClosingBalance().subtract(systemEndingBalance).setScale(2, RoundingMode.HALF_UP))
                .status(ReconciliationStatus.OPEN)
                .lines(new ArrayList<>())
                .build();

        if (request.getStatementLines() != null) {
            for (ReconciliationLineFormDto statementLine : request.getStatementLines()) {
                reconciliation.getLines().add(ReconciliationLine.builder()
                        .reconciliation(reconciliation)
                        .transactionDate(statementLine.getTransactionDate())
                        .description(statementLine.getDescription())
                        .amount(statementLine.getAmount().setScale(2, RoundingMode.HALF_UP))
                        .transactionType(ReconciliationLineSourceType.BANK_STATEMENT)
                        .status(ReconciliationLineStatus.UNMATCHED)
                        .sourceReference(statementLine.getSourceReference())
                        .build());
            }
        }

        List<JournalEntryLine> systemLines = journalEntryLineRepository.findLedgerLines(
                bankAccount.getLinkedAccount().getId(),
                request.getStatementStartDate(),
                request.getStatementEndDate()
        );
        for (JournalEntryLine systemLine : systemLines) {
            BigDecimal amount = systemLine.getDebit().compareTo(BigDecimal.ZERO) > 0 ? systemLine.getDebit() : systemLine.getCredit();
            reconciliation.getLines().add(ReconciliationLine.builder()
                    .reconciliation(reconciliation)
                    .transactionDate(systemLine.getJournalEntry().getEntryDate())
                    .description(systemLine.getDescription())
                    .amount(amount.setScale(2, RoundingMode.HALF_UP))
                    .transactionType(ReconciliationLineSourceType.SYSTEM_TRANSACTION)
                    .status(ReconciliationLineStatus.UNMATCHED)
                    .sourceReference(systemLine.getJournalEntry().getReferenceNumber())
                    .journalEntryLineId(systemLine.getId())
                    .build());
        }

        return toDisplay(reconciliationRepository.save(reconciliation));
    }

    @Transactional
    public ReconciliationDisplayDto matchLines(Long reconciliationId, Long statementLineId, Long systemLineId) {
        Reconciliation reconciliation = loadReconciliation(reconciliationId);
        ensureOpen(reconciliation);
        ReconciliationLine statementLine = loadLine(reconciliationId, statementLineId);
        ReconciliationLine systemLine = loadLine(reconciliationId, systemLineId);

        if (statementLine.getTransactionType() != ReconciliationLineSourceType.BANK_STATEMENT
                || systemLine.getTransactionType() != ReconciliationLineSourceType.SYSTEM_TRANSACTION) {
            throw new BusinessException("Matching must pair a statement line with a system transaction line");
        }

        BigDecimal matchedAmount = statementLine.getAmount().min(systemLine.getAmount()).setScale(2, RoundingMode.HALF_UP);
        ReconciliationLineStatus status = statementLine.getAmount().compareTo(systemLine.getAmount()) == 0
                ? ReconciliationLineStatus.MATCHED
                : ReconciliationLineStatus.PARTIALLY_MATCHED;

        statementLine.setStatus(status);
        systemLine.setStatus(status);
        statementLine.setMatchedLineId(systemLine.getId());
        systemLine.setMatchedLineId(statementLine.getId());
        statementLine.setMatchedAmount(matchedAmount);
        systemLine.setMatchedAmount(matchedAmount);

        reconciliationLineRepository.save(statementLine);
        reconciliationLineRepository.save(systemLine);
        return toDisplay(reconciliation);
    }

    @Transactional
    public ReconciliationDisplayDto unmatchLine(Long reconciliationId, Long lineId) {
        Reconciliation reconciliation = loadReconciliation(reconciliationId);
        ensureOpen(reconciliation);
        ReconciliationLine line = loadLine(reconciliationId, lineId);
        if (line.getMatchedLineId() != null) {
            ReconciliationLine counterpart = loadLine(reconciliationId, line.getMatchedLineId());
            counterpart.setMatchedLineId(null);
            counterpart.setMatchedAmount(null);
            counterpart.setStatus(ReconciliationLineStatus.UNMATCHED);
            reconciliationLineRepository.save(counterpart);
        }
        line.setMatchedLineId(null);
        line.setMatchedAmount(null);
        line.setStatus(ReconciliationLineStatus.UNMATCHED);
        reconciliationLineRepository.save(line);
        return toDisplay(reconciliation);
    }

    @Transactional
    public ReconciliationDisplayDto finalizeReconciliation(Long reconciliationId, String actor) {
        Reconciliation reconciliation = loadReconciliation(reconciliationId);
        ensureOpen(reconciliation);

        long unmatchedStatements = reconciliationLineRepository.findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                reconciliationId, ReconciliationLineSourceType.BANK_STATEMENT).stream()
                .filter(line -> line.getStatus() == ReconciliationLineStatus.UNMATCHED)
                .count();
        if (unmatchedStatements > 0) {
            throw new BusinessException("All statement lines must be matched before finalizing reconciliation");
        }
        if (reconciliation.getDifference().compareTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new BusinessException("Reconciliation difference must be zero before finalization");
        }

        reconciliation.setStatus(ReconciliationStatus.FINALIZED);
        reconciliation.setFinalizedAt(LocalDateTime.now());
        reconciliation.setFinalizedBy(actor);
        return toDisplay(reconciliationRepository.save(reconciliation));
    }

    private Reconciliation loadReconciliation(Long id) {
        return reconciliationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reconciliation", id));
    }

    private ReconciliationLine loadLine(Long reconciliationId, Long id) {
        ReconciliationLine line = reconciliationLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReconciliationLine", id));
        if (!line.getReconciliation().getId().equals(reconciliationId)) {
            throw new BusinessException("Reconciliation line does not belong to the requested reconciliation");
        }
        return line;
    }

    private void ensureOpen(Reconciliation reconciliation) {
        if (reconciliation.getStatus() != ReconciliationStatus.OPEN) {
            throw new BusinessException("Only open reconciliations can be modified");
        }
    }

    private ReconciliationDisplayDto toDisplay(Reconciliation reconciliation) {
        List<ReconciliationLineDisplayDto> lines = reconciliation.getLines().stream()
                .map(this::toLineDisplay)
                .toList();

        long matchedCount = lines.stream().filter(line -> line.getStatus() == ReconciliationLineStatus.MATCHED).count();
        long partiallyMatchedCount = lines.stream().filter(line -> line.getStatus() == ReconciliationLineStatus.PARTIALLY_MATCHED).count();
        long unmatchedCount = lines.stream().filter(line -> line.getStatus() == ReconciliationLineStatus.UNMATCHED).count();

        return ReconciliationDisplayDto.builder()
                .id(reconciliation.getId())
                .bankAccountId(reconciliation.getBankAccount().getId())
                .bankAccountNumber(reconciliation.getBankAccount().getAccountNumber())
                .statementStartDate(reconciliation.getStatementStartDate())
                .statementEndDate(reconciliation.getStatementEndDate())
                .openingBalance(reconciliation.getOpeningBalance())
                .closingBalance(reconciliation.getClosingBalance())
                .systemEndingBalance(reconciliation.getSystemEndingBalance())
                .difference(reconciliation.getDifference())
                .status(reconciliation.getStatus())
                .matchedCount(matchedCount)
                .partiallyMatchedCount(partiallyMatchedCount)
                .unmatchedCount(unmatchedCount)
                .finalizedAt(reconciliation.getFinalizedAt())
                .finalizedBy(reconciliation.getFinalizedBy())
                .lines(lines)
                .createdAt(reconciliation.getCreatedAt())
                .updatedAt(reconciliation.getUpdatedAt())
                .build();
    }

    private ReconciliationLineDisplayDto toLineDisplay(ReconciliationLine line) {
        return ReconciliationLineDisplayDto.builder()
                .id(line.getId())
                .transactionDate(line.getTransactionDate())
                .description(line.getDescription())
                .amount(line.getAmount())
                .transactionType(line.getTransactionType())
                .status(line.getStatus())
                .sourceReference(line.getSourceReference())
                .journalEntryLineId(line.getJournalEntryLineId())
                .matchedLineId(line.getMatchedLineId())
                .matchedAmount(line.getMatchedAmount())
                .build();
    }
}
