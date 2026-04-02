package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.BankAccount;
import com.erp.system.accounting.domain.Reconciliation;
import com.erp.system.accounting.domain.ReconciliationLine;
import com.erp.system.accounting.domain.ReconciliationMatchPair;
import com.erp.system.accounting.repository.BankAccountRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.accounting.repository.ReconciliationLineRepository;
import com.erp.system.accounting.repository.ReconciliationMatchPairRepository;
import com.erp.system.accounting.repository.ReconciliationRepository;
import com.erp.system.common.enums.ReconciliationLineSourceType;
import com.erp.system.common.enums.ReconciliationLineStatus;
import com.erp.system.common.enums.ReconciliationStatus;
import com.erp.system.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock
    private ReconciliationRepository reconciliationRepository;

    @Mock
    private ReconciliationLineRepository reconciliationLineRepository;

    @Mock
    private ReconciliationMatchPairRepository matchPairRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;

    @Mock
    private AccountingAuditService auditService;

    @InjectMocks
    private ReconciliationService reconciliationService;

    @Test
    void matchLinesUpdatesStatusAndAmounts() {
        Reconciliation recon = buildReconciliation(1L, ReconciliationStatus.OPEN);

        ReconciliationLine stmtLine = buildLine(10L, recon,
                ReconciliationLineSourceType.BANK_STATEMENT, new BigDecimal("100.00"));
        ReconciliationLine sysLine = buildLine(20L, recon,
                ReconciliationLineSourceType.SYSTEM_TRANSACTION, new BigDecimal("100.00"));

        recon.setLines(new ArrayList<>(List.of(stmtLine, sysLine)));

        when(reconciliationRepository.findById(1L)).thenReturn(Optional.of(recon));
        when(reconciliationLineRepository.findById(10L)).thenReturn(Optional.of(stmtLine));
        when(reconciliationLineRepository.findById(20L)).thenReturn(Optional.of(sysLine));
        when(reconciliationLineRepository.save(any(ReconciliationLine.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reconciliationRepository.save(any(Reconciliation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(matchPairRepository.save(any(ReconciliationMatchPair.class))).thenAnswer(inv -> inv.getArgument(0));
        when(matchPairRepository.findAllByLineId(anyLong())).thenReturn(List.of());
        when(reconciliationLineRepository
                .findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        eq(1L), eq(ReconciliationLineSourceType.BANK_STATEMENT)))
                .thenReturn(List.of(stmtLine));
        when(reconciliationLineRepository
                .findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        eq(1L), eq(ReconciliationLineSourceType.SYSTEM_TRANSACTION)))
                .thenReturn(List.of(sysLine));

        reconciliationService.matchLines(1L, 10L, 20L, "tester");

        assertThat(stmtLine.getStatus()).isEqualTo(ReconciliationLineStatus.MATCHED);
        assertThat(sysLine.getStatus()).isEqualTo(ReconciliationLineStatus.MATCHED);
        assertThat(stmtLine.getMatchedAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(sysLine.getMatchedAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(stmtLine.getMatchedLineId()).isEqualTo(20L);
        assertThat(sysLine.getMatchedLineId()).isEqualTo(10L);
        verify(matchPairRepository).save(any(ReconciliationMatchPair.class));
    }

    @Test
    void matchAlreadyMatchedLineThrows() {
        Reconciliation recon = buildReconciliation(1L, ReconciliationStatus.OPEN);

        ReconciliationLine stmtLine = buildLine(10L, recon,
                ReconciliationLineSourceType.BANK_STATEMENT, new BigDecimal("100.00"));
        stmtLine.setStatus(ReconciliationLineStatus.MATCHED);

        ReconciliationLine sysLine = buildLine(20L, recon,
                ReconciliationLineSourceType.SYSTEM_TRANSACTION, new BigDecimal("100.00"));

        when(reconciliationRepository.findById(1L)).thenReturn(Optional.of(recon));
        when(reconciliationLineRepository.findById(10L)).thenReturn(Optional.of(stmtLine));
        when(reconciliationLineRepository.findById(20L)).thenReturn(Optional.of(sysLine));

        assertThatThrownBy(() -> reconciliationService.matchLines(1L, 10L, 20L, "tester"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already fully matched");
    }

    @Test
    void matchWrongTypeThrows() {
        Reconciliation recon = buildReconciliation(1L, ReconciliationStatus.OPEN);

        ReconciliationLine stmt1 = buildLine(10L, recon,
                ReconciliationLineSourceType.BANK_STATEMENT, new BigDecimal("100.00"));
        ReconciliationLine stmt2 = buildLine(20L, recon,
                ReconciliationLineSourceType.BANK_STATEMENT, new BigDecimal("100.00"));

        when(reconciliationRepository.findById(1L)).thenReturn(Optional.of(recon));
        when(reconciliationLineRepository.findById(10L)).thenReturn(Optional.of(stmt1));
        when(reconciliationLineRepository.findById(20L)).thenReturn(Optional.of(stmt2));

        assertThatThrownBy(() -> reconciliationService.matchLines(1L, 10L, 20L, "tester"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("statement line with a system transaction");
    }

    @Test
    void unmatchLineClearsViaMatchPairs() {
        Reconciliation recon = buildReconciliation(1L, ReconciliationStatus.IN_PROGRESS);

        ReconciliationLine stmtLine = buildLine(10L, recon,
                ReconciliationLineSourceType.BANK_STATEMENT, new BigDecimal("100.00"));
        stmtLine.setStatus(ReconciliationLineStatus.MATCHED);
        stmtLine.setMatchedLineId(20L);
        stmtLine.setMatchedAmount(new BigDecimal("100.00"));
        stmtLine.setRemainingAmount(BigDecimal.ZERO);

        ReconciliationLine sysLine = buildLine(20L, recon,
                ReconciliationLineSourceType.SYSTEM_TRANSACTION, new BigDecimal("100.00"));
        sysLine.setStatus(ReconciliationLineStatus.MATCHED);
        sysLine.setMatchedLineId(10L);
        sysLine.setMatchedAmount(new BigDecimal("100.00"));
        sysLine.setRemainingAmount(BigDecimal.ZERO);

        recon.setLines(new ArrayList<>(List.of(stmtLine, sysLine)));

        ReconciliationMatchPair pair = ReconciliationMatchPair.builder()
                .id(1L)
                .reconciliation(recon)
                .statementLine(stmtLine)
                .systemLine(sysLine)
                .matchedAmount(new BigDecimal("100.00"))
                .matchedBy("tester")
                .active(true)
                .build();

        when(reconciliationRepository.findById(1L)).thenReturn(Optional.of(recon));
        when(matchPairRepository.findActiveByLineId(10L)).thenReturn(List.of(pair));
        when(matchPairRepository.save(any(ReconciliationMatchPair.class))).thenAnswer(inv -> inv.getArgument(0));
        when(matchPairRepository.findAllByLineId(anyLong())).thenReturn(List.of());
        when(reconciliationLineRepository.save(any(ReconciliationLine.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reconciliationRepository.save(any(Reconciliation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reconciliationLineRepository
                .findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        eq(1L), eq(ReconciliationLineSourceType.BANK_STATEMENT)))
                .thenReturn(List.of(stmtLine));
        when(reconciliationLineRepository
                .findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        eq(1L), eq(ReconciliationLineSourceType.SYSTEM_TRANSACTION)))
                .thenReturn(List.of(sysLine));

        reconciliationService.unmatchLine(1L, 10L, "admin");

        assertThat(stmtLine.getStatus()).isEqualTo(ReconciliationLineStatus.UNMATCHED);
        assertThat(sysLine.getStatus()).isEqualTo(ReconciliationLineStatus.UNMATCHED);
        assertThat(pair.isActive()).isFalse();
        assertThat(pair.getUnmatchedBy()).isEqualTo("admin");
    }

    @Test
    void finalizeWithUnmatchedStatementThrows() {
        Reconciliation recon = buildReconciliation(1L, ReconciliationStatus.IN_PROGRESS);

        ReconciliationLine unmatchedStmt = buildLine(10L, recon,
                ReconciliationLineSourceType.BANK_STATEMENT, new BigDecimal("100.00"));

        when(reconciliationRepository.findById(1L)).thenReturn(Optional.of(recon));
        when(reconciliationLineRepository
                .findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        eq(1L), eq(ReconciliationLineSourceType.BANK_STATEMENT)))
                .thenReturn(List.of(unmatchedStmt));

        assertThatThrownBy(() -> reconciliationService.finalizeReconciliation(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("must be matched");
    }

    @Test
    void finalizeWithNonZeroDifferenceThrows() {
        Reconciliation recon = buildReconciliation(1L, ReconciliationStatus.IN_PROGRESS);
        recon.setDifference(new BigDecimal("50.00"));

        when(reconciliationRepository.findById(1L)).thenReturn(Optional.of(recon));
        when(reconciliationLineRepository
                .findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        eq(1L), eq(ReconciliationLineSourceType.BANK_STATEMENT)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> reconciliationService.finalizeReconciliation(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("difference must be zero");
    }

    @Test
    void finalizeSuccessSetsCompleted() {
        Reconciliation recon = buildReconciliation(1L, ReconciliationStatus.IN_PROGRESS);
        recon.setDifference(new BigDecimal("0.00"));

        ReconciliationLine matchedStmt = buildLine(10L, recon,
                ReconciliationLineSourceType.BANK_STATEMENT, new BigDecimal("100.00"));
        matchedStmt.setStatus(ReconciliationLineStatus.MATCHED);

        recon.setLines(new ArrayList<>(List.of(matchedStmt)));

        when(reconciliationRepository.findById(1L)).thenReturn(Optional.of(recon));
        when(reconciliationLineRepository
                .findByReconciliationIdAndTransactionTypeOrderByTransactionDateAscIdAsc(
                        eq(1L), eq(ReconciliationLineSourceType.BANK_STATEMENT)))
                .thenReturn(List.of(matchedStmt));
        when(reconciliationRepository.save(any(Reconciliation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(matchPairRepository.findAllByLineId(anyLong())).thenReturn(List.of());

        reconciliationService.finalizeReconciliation(1L, "admin");

        assertThat(recon.getStatus()).isEqualTo(ReconciliationStatus.COMPLETED);
        assertThat(recon.getFinalizedBy()).isEqualTo("admin");
        assertThat(recon.getFinalizedAt()).isNotNull();
    }

    @Test
    void modifyCompletedReconciliationThrows() {
        Reconciliation recon = buildReconciliation(1L, ReconciliationStatus.COMPLETED);

        when(reconciliationRepository.findById(1L)).thenReturn(Optional.of(recon));

        assertThatThrownBy(() -> reconciliationService.matchLines(1L, 10L, 20L, "tester"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only open or in-progress");
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private Reconciliation buildReconciliation(Long id, ReconciliationStatus status) {
        BankAccount bankAccount = BankAccount.builder()
                .id(1L)
                .bankName("Test Bank")
                .accountNumber("123-456")
                .currency("USD")
                .build();

        Reconciliation recon = new Reconciliation();
        recon.setId(id);
        recon.setBankAccount(bankAccount);
        recon.setStatementStartDate(LocalDate.of(2026, 1, 1));
        recon.setStatementEndDate(LocalDate.of(2026, 1, 31));
        recon.setOpeningBalance(new BigDecimal("1000.00"));
        recon.setClosingBalance(new BigDecimal("1200.00"));
        recon.setSystemEndingBalance(new BigDecimal("1200.00"));
        recon.setDifference(BigDecimal.ZERO.setScale(2));
        recon.setStatus(status);
        recon.setLines(new ArrayList<>());
        return recon;
    }

    private ReconciliationLine buildLine(Long id, Reconciliation recon,
                                         ReconciliationLineSourceType type, BigDecimal amount) {
        ReconciliationLine line = new ReconciliationLine();
        line.setId(id);
        line.setReconciliation(recon);
        line.setTransactionDate(LocalDate.of(2026, 1, 15));
        line.setDescription("Test line " + id);
        line.setAmount(amount);
        line.setTransactionType(type);
        line.setStatus(ReconciliationLineStatus.UNMATCHED);
        return line;
    }
}
