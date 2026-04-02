package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.BankAccount;
import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.accounting.domain.Reconciliation;
import com.erp.system.accounting.domain.ReconciliationLine;
import com.erp.system.accounting.domain.ReconciliationMatchPair;
import com.erp.system.accounting.dto.display.ReconciliationDisplayDto;
import com.erp.system.accounting.dto.display.ReconciliationBankAccountDto;
import com.erp.system.accounting.dto.display.ReconciliationLineDisplayDto;
import com.erp.system.accounting.dto.display.ReconciliationMatchPairDisplayDto;
import com.erp.system.accounting.dto.display.ReconciliationSummaryDto;
import com.erp.system.accounting.dto.form.ReconciliationFormDto;
import com.erp.system.accounting.dto.form.ReconciliationLineFormDto;
import com.erp.system.accounting.repository.BankAccountRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.accounting.repository.ReconciliationLineRepository;
import com.erp.system.accounting.repository.ReconciliationMatchPairRepository;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ReconciliationRepository reconciliationRepository;
    private final ReconciliationLineRepository reconciliationLineRepository;
    private final ReconciliationMatchPairRepository matchPairRepository;
    private final BankAccountRepository bankAccountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final AccountingAuditService auditService;

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
                        .currentBalance(currentBankBalance(account))
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
                        reconciliationId, ReconciliationLineSourceType.BANK_STATEMENT)
                .stream()
                .map(this::toLineDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReconciliationLineDisplayDto> getSystemTransactions(Long reconciliationId) {
        return reconciliationLineRepository.findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        reconciliationId, ReconciliationLineSourceType.SYSTEM_TRANSACTION)
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

    @Transactional(readOnly = true)
    public List<ReconciliationMatchPairDisplayDto> getMatchHistory(Long reconciliationId) {
        return matchPairRepository.findByReconciliationIdOrderByMatchedAtDesc(reconciliationId)
                .stream()
                .map(this::toMatchPairDisplay)
                .toList();
    }

    @Transactional
    public ReconciliationDisplayDto createReconciliation(ReconciliationFormDto request) {
        if (request.getStatementEndDate().isBefore(request.getStatementStartDate())) {
            throw new BusinessException("Statement end date cannot be before start date");
        }
        BankAccount bankAccount = bankAccountRepository.findById(request.getBankAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", request.getBankAccountId()));

        BigDecimal systemEndingBalance = currentBankBalance(bankAccount).setScale(2, RoundingMode.HALF_UP);

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
                        .remainingAmount(statementLine.getAmount().setScale(2, RoundingMode.HALF_UP))
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
                    .remainingAmount(amount.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return toDisplay(reconciliationRepository.save(reconciliation));
    }

    @Transactional
    public ReconciliationDisplayDto matchLines(Long reconciliationId, Long statementLineId,
                                               Long systemLineId, String actor) {
        Reconciliation reconciliation = loadReconciliation(reconciliationId);
        ensureOpenOrInProgress(reconciliation);
        ReconciliationLine statementLine = loadLine(reconciliationId, statementLineId);
        ReconciliationLine systemLine = loadLine(reconciliationId, systemLineId);

        if (statementLine.getTransactionType() != ReconciliationLineSourceType.BANK_STATEMENT
                || systemLine.getTransactionType() != ReconciliationLineSourceType.SYSTEM_TRANSACTION) {
            throw new BusinessException("Matching must pair a statement line with a system transaction line");
        }

        if (statementLine.getStatus() == ReconciliationLineStatus.MATCHED
                || systemLine.getStatus() == ReconciliationLineStatus.MATCHED) {
            throw new BusinessException("Cannot match a line that is already fully matched");
        }

        BigDecimal stmtRemaining = statementLine.getRemainingAmount() != null
                ? statementLine.getRemainingAmount() : statementLine.getAmount();
        BigDecimal sysRemaining = systemLine.getRemainingAmount() != null
                ? systemLine.getRemainingAmount() : systemLine.getAmount();

        BigDecimal matchedAmount = stmtRemaining.min(sysRemaining).setScale(2, RoundingMode.HALF_UP);

        BigDecimal newStmtRemaining = stmtRemaining.subtract(matchedAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newSysRemaining = sysRemaining.subtract(matchedAmount).setScale(2, RoundingMode.HALF_UP);

        statementLine.setMatchedAmount(
                (statementLine.getMatchedAmount() != null ? statementLine.getMatchedAmount() : BigDecimal.ZERO)
                        .add(matchedAmount).setScale(2, RoundingMode.HALF_UP));
        systemLine.setMatchedAmount(
                (systemLine.getMatchedAmount() != null ? systemLine.getMatchedAmount() : BigDecimal.ZERO)
                        .add(matchedAmount).setScale(2, RoundingMode.HALF_UP));

        statementLine.setRemainingAmount(newStmtRemaining);
        systemLine.setRemainingAmount(newSysRemaining);

        statementLine.setStatus(newStmtRemaining.compareTo(BigDecimal.ZERO) == 0
                ? ReconciliationLineStatus.MATCHED : ReconciliationLineStatus.PARTIALLY_MATCHED);
        systemLine.setStatus(newSysRemaining.compareTo(BigDecimal.ZERO) == 0
                ? ReconciliationLineStatus.MATCHED : ReconciliationLineStatus.PARTIALLY_MATCHED);

        statementLine.setMatchedLineId(systemLine.getId());
        systemLine.setMatchedLineId(statementLine.getId());

        reconciliationLineRepository.save(statementLine);
        reconciliationLineRepository.save(systemLine);

        String matchActor = actor != null ? actor : "system";
        matchPairRepository.save(ReconciliationMatchPair.builder()
                .reconciliation(reconciliation)
                .statementLine(statementLine)
                .systemLine(systemLine)
                .matchedAmount(matchedAmount)
                .matchedAt(Instant.now())
                .matchedBy(matchActor)
                .active(true)
                .build());

        reconciliation.setStatus(ReconciliationStatus.IN_PROGRESS);
        reconciliationRepository.save(reconciliation);
        recalculateDifference(reconciliation);

        auditService.log("Reconciliation", reconciliationId, "MATCH", matchActor,
                "Matched stmt=" + statementLineId + " sys=" + systemLineId + " amount=" + matchedAmount);
        return toDisplay(reconciliation);
    }

    @Transactional
    public ReconciliationDisplayDto unmatchLine(Long reconciliationId, Long lineId, String actor) {
        Reconciliation reconciliation = loadReconciliation(reconciliationId);
        ensureOpenOrInProgress(reconciliation);

        String unmatchActor = actor != null ? actor : "system";
        List<ReconciliationMatchPair> activePairs = matchPairRepository.findActiveByLineId(lineId);

        for (ReconciliationMatchPair pair : activePairs) {
            if (!pair.getReconciliation().getId().equals(reconciliationId)) {
                continue;
            }
            deactivatePair(pair, unmatchActor);
        }

        if (activePairs.isEmpty()) {
            ReconciliationLine line = loadLine(reconciliationId, lineId);
            if (line.getMatchedLineId() != null) {
                ReconciliationLine counterpart = loadLine(reconciliationId, line.getMatchedLineId());
                resetLine(counterpart);
                reconciliationLineRepository.save(counterpart);
            }
            resetLine(line);
            reconciliationLineRepository.save(line);
        }

        reconciliation.setStatus(ReconciliationStatus.IN_PROGRESS);
        reconciliationRepository.save(reconciliation);
        recalculateDifference(reconciliation);

        auditService.log("Reconciliation", reconciliationId, "UNMATCH", unmatchActor,
                "Unmatched line " + lineId);
        return toDisplay(reconciliation);
    }

    @Transactional
    public ReconciliationDisplayDto unmatchPair(Long reconciliationId, Long matchPairId, String actor) {
        Reconciliation reconciliation = loadReconciliation(reconciliationId);
        ensureOpenOrInProgress(reconciliation);

        ReconciliationMatchPair pair = matchPairRepository.findById(matchPairId)
                .orElseThrow(() -> new ResourceNotFoundException("ReconciliationMatchPair", matchPairId));
        if (!pair.getReconciliation().getId().equals(reconciliationId)) {
            throw new BusinessException("Match pair does not belong to the requested reconciliation");
        }
        if (!pair.isActive()) {
            throw new BusinessException("Match pair is already inactive");
        }

        String unmatchActor = actor != null ? actor : "system";
        deactivatePair(pair, unmatchActor);

        reconciliation.setStatus(ReconciliationStatus.IN_PROGRESS);
        reconciliationRepository.save(reconciliation);
        recalculateDifference(reconciliation);

        auditService.log("Reconciliation", reconciliationId, "UNMATCH_PAIR", unmatchActor,
                "Unmatched pair " + matchPairId + " amount=" + pair.getMatchedAmount());
        return toDisplay(reconciliation);
    }

    @Transactional
    public ReconciliationDisplayDto finalizeReconciliation(Long reconciliationId, String actor) {
        Reconciliation reconciliation = loadReconciliation(reconciliationId);
        ensureOpenOrInProgress(reconciliation);

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

        reconciliation.setStatus(ReconciliationStatus.COMPLETED);
        reconciliation.setFinalizedAt(LocalDateTime.now());
        reconciliation.setFinalizedBy(actor);
        Reconciliation saved = reconciliationRepository.save(reconciliation);
        auditService.log("Reconciliation", reconciliationId, "FINALIZE", actor,
                "Finalized reconciliation");
        return toDisplay(saved);
    }

    @Transactional
    public ReconciliationDisplayDto cancelReconciliation(Long reconciliationId, String actor) {
        Reconciliation reconciliation = loadReconciliation(reconciliationId);
        if (reconciliation.getStatus() == ReconciliationStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed reconciliation");
        }
        if (reconciliation.getStatus() == ReconciliationStatus.CANCELLED) {
            throw new BusinessException("Reconciliation is already cancelled");
        }
        reconciliation.setStatus(ReconciliationStatus.CANCELLED);
        reconciliation.setFinalizedAt(LocalDateTime.now());
        reconciliation.setFinalizedBy(actor);
        Reconciliation saved = reconciliationRepository.save(reconciliation);
        auditService.log("Reconciliation", reconciliationId, "CANCEL", actor,
                "Cancelled reconciliation");
        return toDisplay(saved);
    }

    // ===================== PRIVATE HELPERS =====================

    private void deactivatePair(ReconciliationMatchPair pair, String actor) {
        pair.setActive(false);
        pair.setUnmatchedAt(Instant.now());
        pair.setUnmatchedBy(actor);
        matchPairRepository.save(pair);

        ReconciliationLine stmtLine = pair.getStatementLine();
        ReconciliationLine sysLine = pair.getSystemLine();

        stmtLine.setMatchedAmount(
                (stmtLine.getMatchedAmount() != null ? stmtLine.getMatchedAmount() : BigDecimal.ZERO)
                        .subtract(pair.getMatchedAmount()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        stmtLine.setRemainingAmount(
                (stmtLine.getRemainingAmount() != null ? stmtLine.getRemainingAmount() : BigDecimal.ZERO)
                        .add(pair.getMatchedAmount()).setScale(2, RoundingMode.HALF_UP));

        sysLine.setMatchedAmount(
                (sysLine.getMatchedAmount() != null ? sysLine.getMatchedAmount() : BigDecimal.ZERO)
                        .subtract(pair.getMatchedAmount()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        sysLine.setRemainingAmount(
                (sysLine.getRemainingAmount() != null ? sysLine.getRemainingAmount() : BigDecimal.ZERO)
                        .add(pair.getMatchedAmount()).setScale(2, RoundingMode.HALF_UP));

        recalculateLineStatus(stmtLine);
        recalculateLineStatus(sysLine);

        reconciliationLineRepository.save(stmtLine);
        reconciliationLineRepository.save(sysLine);
    }

    private void recalculateLineStatus(ReconciliationLine line) {
        if (line.getMatchedAmount() == null || line.getMatchedAmount().compareTo(BigDecimal.ZERO) == 0) {
            line.setStatus(ReconciliationLineStatus.UNMATCHED);
            line.setMatchedLineId(null);
        } else if (line.getRemainingAmount() != null && line.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            line.setStatus(ReconciliationLineStatus.MATCHED);
        } else {
            line.setStatus(ReconciliationLineStatus.PARTIALLY_MATCHED);
        }
    }

    private void resetLine(ReconciliationLine line) {
        line.setMatchedLineId(null);
        line.setMatchedAmount(null);
        line.setRemainingAmount(line.getAmount());
        line.setStatus(ReconciliationLineStatus.UNMATCHED);
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

    private void ensureOpenOrInProgress(Reconciliation reconciliation) {
        if (reconciliation.getStatus() != ReconciliationStatus.OPEN && reconciliation.getStatus() != ReconciliationStatus.IN_PROGRESS) {
            throw new BusinessException("Only open or in-progress reconciliations can be modified");
        }
    }

    private void recalculateDifference(Reconciliation reconciliation) {
        BigDecimal matchedStatementTotal = reconciliationLineRepository
                .findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        reconciliation.getId(), ReconciliationLineSourceType.BANK_STATEMENT)
                .stream()
                .filter(l -> l.getStatus() == ReconciliationLineStatus.MATCHED)
                .map(ReconciliationLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal matchedSystemTotal = reconciliationLineRepository
                .findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        reconciliation.getId(), ReconciliationLineSourceType.SYSTEM_TRANSACTION)
                .stream()
                .filter(l -> l.getStatus() == ReconciliationLineStatus.MATCHED)
                .map(ReconciliationLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal adjustedSystemBalance = reconciliation.getSystemEndingBalance()
                .add(matchedStatementTotal.subtract(matchedSystemTotal))
                .setScale(2, RoundingMode.HALF_UP);
        reconciliation.setDifference(
                reconciliation.getClosingBalance().subtract(adjustedSystemBalance).setScale(2, RoundingMode.HALF_UP));
        reconciliationRepository.save(reconciliation);
    }

    // ===================== DISPLAY MAPPERS =====================

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
        List<ReconciliationMatchPairDisplayDto> pairs = matchPairRepository.findAllByLineId(line.getId())
                .stream()
                .map(this::toMatchPairDisplay)
                .toList();

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
                .remainingAmount(line.getRemainingAmount())
                .matchPairs(pairs)
                .build();
    }

    private ReconciliationMatchPairDisplayDto toMatchPairDisplay(ReconciliationMatchPair pair) {
        return ReconciliationMatchPairDisplayDto.builder()
                .id(pair.getId())
                .reconciliationId(pair.getReconciliation().getId())
                .statementLineId(pair.getStatementLine().getId())
                .systemLineId(pair.getSystemLine().getId())
                .matchedAmount(pair.getMatchedAmount())
                .matchedAt(pair.getMatchedAt())
                .matchedBy(pair.getMatchedBy())
                .active(pair.isActive())
                .unmatchedAt(pair.getUnmatchedAt())
                .unmatchedBy(pair.getUnmatchedBy())
                .build();
    }

    private BigDecimal currentBankBalance(BankAccount bankAccount) {
        BigDecimal openingBalance = bankAccount.getOpeningBalance() == null ? BigDecimal.ZERO : bankAccount.getOpeningBalance();
        return openingBalance.add(journalEntryLineRepository.sumNetMovementByAccountId(bankAccount.getLinkedAccount().getId()));
    }
}
