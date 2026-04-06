package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.accounting.dto.display.LedgerDisplayDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.common.enums.AccountingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;

    @InjectMocks
    private LedgerService ledgerService;

    @Test
    void ledgerOpeningBalanceIsSignedOpeningBalance() {
        Account account = Account.builder()
                .id(1L)
                .code("1001")
                .nameEn("Cash")
                .accountType(AccountingType.ASSET)
                .openingBalance(new BigDecimal("1000"))
                .openingBalanceSide(Account.BalanceSide.DEBIT)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findLedgerLeafAccountsAmongIds(List.of(1L))).thenReturn(List.of(account));
        when(journalEntryLineRepository.findLedgerLinesForAccountIds(eq(List.of(1L)), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        LedgerDisplayDto ledger = ledgerService.getLedger(1L, null, null);

        assertThat(ledger.getOpeningBalance()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(ledger.getClosingBalance()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(ledger.getLines()).isEmpty();
    }

    @Test
    void ledgerRunningBalanceCalculation() {
        Account account = Account.builder()
                .id(1L)
                .code("1001")
                .nameEn("Cash")
                .accountType(AccountingType.ASSET)
                .openingBalance(new BigDecimal("500"))
                .openingBalanceSide(Account.BalanceSide.DEBIT)
                .build();

        JournalEntry je1 = buildJournalEntry(10L, "JE-010", LocalDate.of(2026, 1, 5));
        JournalEntry je2 = buildJournalEntry(11L, "JE-011", LocalDate.of(2026, 1, 10));

        JournalEntryLine debitLine = JournalEntryLine.builder()
                .id(100L)
                .journalEntry(je1)
                .account(account)
                .description("Debit 200")
                .debit(new BigDecimal("200"))
                .credit(BigDecimal.ZERO)
                .lineNumber(1)
                .build();

        JournalEntryLine creditLine = JournalEntryLine.builder()
                .id(101L)
                .journalEntry(je2)
                .account(account)
                .description("Credit 100")
                .debit(BigDecimal.ZERO)
                .credit(new BigDecimal("100"))
                .lineNumber(1)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findLedgerLeafAccountsAmongIds(List.of(1L))).thenReturn(List.of(account));
        when(journalEntryLineRepository.findLedgerLinesForAccountIds(eq(List.of(1L)), isNull(), isNull()))
                .thenReturn(List.of(debitLine, creditLine));

        LedgerDisplayDto ledger = ledgerService.getLedger(1L, null, null);

        assertThat(ledger.getOpeningBalance()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(ledger.getClosingBalance()).isEqualByComparingTo(new BigDecimal("600"));
        assertThat(ledger.getLines()).hasSize(2);
        assertThat(ledger.getLines().get(0).getRunningBalance()).isEqualByComparingTo(new BigDecimal("700"));
        assertThat(ledger.getLines().get(1).getRunningBalance()).isEqualByComparingTo(new BigDecimal("600"));
    }

    @Test
    void ledgerWithFromDateAdjustsOpeningBalance() {
        Account account = Account.builder()
                .id(1L)
                .code("1001")
                .nameEn("Cash")
                .accountType(AccountingType.ASSET)
                .openingBalance(new BigDecimal("1000"))
                .openingBalanceSide(Account.BalanceSide.DEBIT)
                .build();

        LocalDate fromDate = LocalDate.of(2026, 3, 1);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findLedgerLeafAccountsAmongIds(List.of(1L))).thenReturn(List.of(account));
        when(journalEntryLineRepository.sumNetMovementBeforeAccountIds(eq(List.of(1L)), eq(fromDate)))
                .thenReturn(new BigDecimal("300"));
        when(journalEntryLineRepository.findLedgerLinesForAccountIds(eq(List.of(1L)), eq(fromDate), isNull()))
                .thenReturn(Collections.emptyList());

        LedgerDisplayDto ledger = ledgerService.getLedger(1L, fromDate, null);

        assertThat(ledger.getOpeningBalance()).isEqualByComparingTo(new BigDecimal("1300"));
        assertThat(ledger.getClosingBalance()).isEqualByComparingTo(new BigDecimal("1300"));
    }

    @Test
    void ledgerReversalEffectIsNeutral() {
        Account account = Account.builder()
                .id(1L)
                .code("1001")
                .nameEn("Cash")
                .accountType(AccountingType.ASSET)
                .openingBalance(new BigDecimal("500"))
                .openingBalanceSide(Account.BalanceSide.DEBIT)
                .build();

        JournalEntry originalJe = buildJournalEntry(20L, "JE-020", LocalDate.of(2026, 2, 1));
        JournalEntry reversalJe = buildJournalEntry(21L, "JE-021", LocalDate.of(2026, 2, 2));

        JournalEntryLine originalLine = JournalEntryLine.builder()
                .id(200L)
                .journalEntry(originalJe)
                .account(account)
                .description("Original debit")
                .debit(new BigDecimal("300"))
                .credit(BigDecimal.ZERO)
                .lineNumber(1)
                .build();

        JournalEntryLine reversalLine = JournalEntryLine.builder()
                .id(201L)
                .journalEntry(reversalJe)
                .account(account)
                .description("Reversal credit")
                .debit(BigDecimal.ZERO)
                .credit(new BigDecimal("300"))
                .lineNumber(1)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findLedgerLeafAccountsAmongIds(List.of(1L))).thenReturn(List.of(account));
        when(journalEntryLineRepository.findLedgerLinesForAccountIds(eq(List.of(1L)), isNull(), isNull()))
                .thenReturn(List.of(originalLine, reversalLine));

        LedgerDisplayDto ledger = ledgerService.getLedger(1L, null, null);

        assertThat(ledger.getOpeningBalance()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(ledger.getClosingBalance()).isEqualByComparingTo(ledger.getOpeningBalance());
        assertThat(ledger.getLines()).hasSize(2);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private JournalEntry buildJournalEntry(Long id, String reference, LocalDate entryDate) {
        JournalEntry je = new JournalEntry();
        je.setId(id);
        je.setReferenceNumber(reference);
        je.setEntryDate(entryDate);
        return je;
    }
}
