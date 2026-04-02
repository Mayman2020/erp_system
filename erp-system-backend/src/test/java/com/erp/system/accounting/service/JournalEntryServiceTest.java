package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.domain.JournalEntryLine;
import com.erp.system.accounting.dto.display.JournalEntryDisplayDto;
import com.erp.system.accounting.dto.display.JournalEntryLineDisplayDto;
import com.erp.system.accounting.dto.form.JournalEntryFormDto;
import com.erp.system.accounting.dto.form.JournalEntryLineFormDto;
import com.erp.system.accounting.mapper.JournalEntryMapper;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryRepository;
import com.erp.system.common.enums.JournalEntryStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.accounting.service.AccountingAuditService;
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
class JournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JournalEntryMapper journalEntryMapper;

    @Mock
    private NumberingService numberingService;

    @Mock
    private PostingPeriodService postingPeriodService;

    @Mock
    private AccountingAuditService auditService;

    @InjectMocks
    private JournalEntryService journalEntryService;

    @Test
    void createValidBalancedJournal_succeeds() {
        Account debitAccount = buildAccount(1L, "1001", "Cash", true, true);
        Account creditAccount = buildAccount(2L, "2001", "Revenue", true, true);

        JournalEntryFormDto form = new JournalEntryFormDto();
        form.setEntryDate(LocalDate.of(2026, 1, 15));
        form.setDescription("Test balanced entry");
        form.setLines(List.of(
                buildLineForm(1L, new BigDecimal("100"), BigDecimal.ZERO),
                buildLineForm(2L, BigDecimal.ZERO, new BigDecimal("100"))
        ));

        when(numberingService.generateNextNumber("JOURNAL_REFERENCE")).thenReturn("JE-0001");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(debitAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(creditAccount));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(journalEntryMapper.toDisplay(any(JournalEntry.class))).thenAnswer(invocation -> {
            JournalEntry je = invocation.getArgument(0);
            return JournalEntryDisplayDto.builder()
                    .referenceNumber(je.getReferenceNumber())
                    .status(je.getStatus())
                    .totalDebit(je.getTotalDebit())
                    .totalCredit(je.getTotalCredit())
                    .entryDate(je.getEntryDate())
                    .build();
        });

        JournalEntryDisplayDto result = journalEntryService.createJournalEntry(form);

        assertThat(result.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
        assertThat(result.getTotalDebit()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(result.getTotalCredit()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(result.getReferenceNumber()).isEqualTo("JE-0001");
        verify(journalEntryRepository).save(any(JournalEntry.class));
    }

    @Test
    void createUnbalancedJournal_throwsBusinessException() {
        Account debitAccount = buildAccount(1L, "1001", "Cash", true, true);
        Account creditAccount = buildAccount(2L, "2001", "Revenue", true, true);

        JournalEntryFormDto form = new JournalEntryFormDto();
        form.setEntryDate(LocalDate.of(2026, 1, 15));
        form.setDescription("Unbalanced entry");
        form.setLines(List.of(
                buildLineForm(1L, new BigDecimal("100"), BigDecimal.ZERO),
                buildLineForm(2L, BigDecimal.ZERO, new BigDecimal("50"))
        ));

        when(numberingService.generateNextNumber("JOURNAL_REFERENCE")).thenReturn("JE-0002");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(debitAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(creditAccount));

        assertThatThrownBy(() -> journalEntryService.createJournalEntry(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not balanced");
    }

    @Test
    void createJournalWithSingleLine_throwsBusinessException() {
        JournalEntryFormDto form = new JournalEntryFormDto();
        form.setEntryDate(LocalDate.of(2026, 1, 15));
        form.setLines(List.of(
                buildLineForm(1L, new BigDecimal("100"), BigDecimal.ZERO)
        ));

        assertThatThrownBy(() -> journalEntryService.createJournalEntry(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("at least two lines");
    }

    @Test
    void postDraftJournal_setsStatusToPosted() {
        JournalEntry draft = buildJournalEntry(1L, JournalEntryStatus.DRAFT,
                new BigDecimal("100.0000"), new BigDecimal("100.0000"));
        draft.setEntryDate(LocalDate.of(2026, 1, 15));

        when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(draft));
        doNothing().when(postingPeriodService).validatePostingDate(any(LocalDate.class));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(journalEntryMapper.toDisplay(any(JournalEntry.class))).thenAnswer(invocation -> {
            JournalEntry je = invocation.getArgument(0);
            return JournalEntryDisplayDto.builder()
                    .status(je.getStatus())
                    .totalDebit(je.getTotalDebit())
                    .totalCredit(je.getTotalCredit())
                    .build();
        });

        JournalEntryDisplayDto result = journalEntryService.postJournalEntry(1L, "admin");

        assertThat(result.getStatus()).isEqualTo(JournalEntryStatus.POSTED);
        assertThat(draft.getPostedBy()).isEqualTo("admin");
        assertThat(draft.getPostedAt()).isNotNull();
    }

    @Test
    void postNonDraftJournal_throwsBusinessException() {
        JournalEntry posted = buildJournalEntry(1L, JournalEntryStatus.POSTED,
                new BigDecimal("100.0000"), new BigDecimal("100.0000"));

        when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(posted));

        assertThatThrownBy(() -> journalEntryService.postJournalEntry(1L, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only draft");
    }

    @Test
    void reversePostedJournal_createsReversalEntry() {
        Account account1 = buildAccount(1L, "1001", "Cash", true, true);
        Account account2 = buildAccount(2L, "2001", "Revenue", true, true);

        JournalEntry original = buildJournalEntry(10L, JournalEntryStatus.POSTED,
                new BigDecimal("200.0000"), new BigDecimal("200.0000"));
        original.setReferenceNumber("JE-ORIG");
        original.setCurrencyCode("USD");
        original.setExternalReference("EXT-001");

        JournalEntryLine line1 = new JournalEntryLine();
        line1.setJournalEntry(original);
        line1.setAccount(account1);
        line1.setDescription("Debit line");
        line1.setDebit(new BigDecimal("200.0000"));
        line1.setCredit(BigDecimal.ZERO);
        line1.setLineNumber(1);

        JournalEntryLine line2 = new JournalEntryLine();
        line2.setJournalEntry(original);
        line2.setAccount(account2);
        line2.setDescription("Credit line");
        line2.setDebit(BigDecimal.ZERO);
        line2.setCredit(new BigDecimal("200.0000"));
        line2.setLineNumber(2);

        original.setLines(new ArrayList<>(List.of(line1, line2)));

        when(journalEntryRepository.findById(10L)).thenReturn(Optional.of(original));
        when(numberingService.generateNextNumber("JOURNAL_REFERENCE")).thenReturn("JE-REV-001");
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(journalEntryMapper.toDisplay(any(JournalEntry.class))).thenAnswer(invocation -> {
            JournalEntry je = invocation.getArgument(0);
            return JournalEntryDisplayDto.builder()
                    .referenceNumber(je.getReferenceNumber())
                    .status(je.getStatus())
                    .totalDebit(je.getTotalDebit())
                    .totalCredit(je.getTotalCredit())
                    .entryType(je.getEntryType())
                    .build();
        });

        JournalEntryDisplayDto result = journalEntryService.reverseJournalEntry(10L, "admin", "Correction");

        assertThat(result.getStatus()).isEqualTo(JournalEntryStatus.POSTED);
        assertThat(result.getEntryType()).isEqualTo("REVERSAL");
        assertThat(result.getReferenceNumber()).isEqualTo("JE-REV-001");

        assertThat(original.getStatus()).isEqualTo(JournalEntryStatus.REVERSED);
        assertThat(original.getReversedBy()).isEqualTo("admin");
        assertThat(original.getReversalReference()).isEqualTo("JE-REV-001");

        verify(journalEntryRepository, times(2)).save(any(JournalEntry.class));
    }

    @Test
    void reverseNonPostedJournal_throwsBusinessException() {
        JournalEntry draft = buildJournalEntry(1L, JournalEntryStatus.DRAFT,
                new BigDecimal("100.0000"), new BigDecimal("100.0000"));

        when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> journalEntryService.reverseJournalEntry(1L, "admin", "reason"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only posted");
    }

    @Test
    void deleteNonDraftJournal_throwsBusinessException() {
        JournalEntry posted = buildJournalEntry(1L, JournalEntryStatus.POSTED,
                new BigDecimal("100.0000"), new BigDecimal("100.0000"));

        when(journalEntryRepository.findById(1L)).thenReturn(Optional.of(posted));

        assertThatThrownBy(() -> journalEntryService.deleteJournalEntry(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only draft");
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private Account buildAccount(Long id, String code, String name, boolean active, boolean postable) {
        return Account.builder()
                .id(id)
                .code(code)
                .nameEn(name)
                .active(active)
                .postable(postable)
                .build();
    }

    private JournalEntryLineFormDto buildLineForm(Long accountId, BigDecimal debit, BigDecimal credit) {
        JournalEntryLineFormDto dto = new JournalEntryLineFormDto();
        dto.setAccountId(accountId);
        dto.setDescription("Line for account " + accountId);
        dto.setDebit(debit);
        dto.setCredit(credit);
        return dto;
    }

    private JournalEntry buildJournalEntry(Long id, JournalEntryStatus status,
                                           BigDecimal totalDebit, BigDecimal totalCredit) {
        JournalEntry je = new JournalEntry();
        je.setId(id);
        je.setReferenceNumber("JE-" + id);
        je.setEntryDate(LocalDate.of(2026, 1, 1));
        je.setStatus(status);
        je.setTotalDebit(totalDebit);
        je.setTotalCredit(totalCredit);
        je.setLines(new ArrayList<>());
        return je;
    }
}
