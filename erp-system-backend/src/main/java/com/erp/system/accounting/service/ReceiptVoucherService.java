package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.domain.ReceiptVoucher;
import com.erp.system.accounting.domain.CustomerInvoice;
import com.erp.system.accounting.dto.display.ReceiptVoucherDisplayDto;
import com.erp.system.accounting.dto.form.ReceiptVoucherFormDto;
import com.erp.system.accounting.mapper.ReceiptVoucherMapper;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.CustomerInvoiceRepository;
import com.erp.system.accounting.repository.ReceiptVoucherRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.enums.PaymentMethod;
import com.erp.system.common.enums.VoucherStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.AccountingSettingsService;
import com.erp.system.common.service.NumberingService;
import com.erp.system.accounting.service.CustomerInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptVoucherService {

    private final ReceiptVoucherRepository receiptVoucherRepository;
    private final AccountRepository accountRepository;
    private final ReceiptVoucherMapper receiptVoucherMapper;
    private final NumberingService numberingService;
    private final AccountingSettingsService accountingSettingsService;
    private final AccountingPostingService accountingPostingService;
    private final CustomerInvoiceService customerInvoiceService;
    private final CustomerInvoiceRepository customerInvoiceRepository;

    @Transactional(readOnly = true)
    public List<ReceiptVoucherDisplayDto> getReceiptVouchers(VoucherStatus status,
                                                             PaymentMethod paymentMethod,
                                                             Long bankAccountId,
                                                             String payer,
                                                             BigDecimal minAmount,
                                                             BigDecimal maxAmount,
                                                             LocalDate fromDate,
                                                             LocalDate toDate,
                                                             String search) {
        List<ReceiptVoucher> vouchers = status == null
                ? receiptVoucherRepository.findAllByOrderByVoucherDateDescIdDesc()
                : receiptVoucherRepository.findByStatusOrderByVoucherDateDescIdDesc(status);

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return vouchers.stream()
                .filter(voucher -> fromDate == null || !voucher.getVoucherDate().isBefore(fromDate))
                .filter(voucher -> toDate == null || !voucher.getVoucherDate().isAfter(toDate))
                .filter(voucher -> paymentMethod == null || voucher.getPaymentMethod() == paymentMethod)
                .filter(voucher -> bankAccountId == null || voucher.getCashAccount().getId().equals(bankAccountId))
                .filter(voucher -> payer == null || payer.isBlank()
                        || (voucher.getPartyName() != null && voucher.getPartyName().toLowerCase().contains(payer.trim().toLowerCase())))
                .filter(voucher -> minAmount == null || voucher.getAmount().compareTo(minAmount) >= 0)
                .filter(voucher -> maxAmount == null || voucher.getAmount().compareTo(maxAmount) <= 0)
                .filter(voucher -> normalizedSearch == null
                        || voucher.getReference().toLowerCase().contains(normalizedSearch)
                        || (voucher.getDescription() != null && voucher.getDescription().toLowerCase().contains(normalizedSearch))
                        || (voucher.getPartyName() != null && voucher.getPartyName().toLowerCase().contains(normalizedSearch))
                        || (voucher.getInvoiceReference() != null && voucher.getInvoiceReference().toLowerCase().contains(normalizedSearch)))
                .map(receiptVoucherMapper::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReceiptVoucherDisplayDto getReceiptVoucher(Long voucherId) {
        return receiptVoucherMapper.toDisplay(loadVoucher(voucherId));
    }

    @Transactional
    public ReceiptVoucherDisplayDto createReceiptVoucher(ReceiptVoucherFormDto request) {
        ReceiptVoucher voucher = ReceiptVoucher.builder()
                .reference(resolveReference(request.getReference()))
                .status(VoucherStatus.DRAFT)
                .build();
        applyForm(voucher, request);
        return receiptVoucherMapper.toDisplay(receiptVoucherRepository.save(voucher));
    }

    @Transactional
    public ReceiptVoucherDisplayDto updateReceiptVoucher(Long voucherId, ReceiptVoucherFormDto request) {
        ReceiptVoucher voucher = loadVoucher(voucherId);
        ensureEditable(voucher);
        applyForm(voucher, request);
        return receiptVoucherMapper.toDisplay(receiptVoucherRepository.save(voucher));
    }

    @Transactional
    public ReceiptVoucherDisplayDto approveReceiptVoucher(Long voucherId, String actor) {
        ReceiptVoucher voucher = loadVoucher(voucherId);
        if (voucher.getStatus() != VoucherStatus.DRAFT) {
            throw new BusinessException("Only draft receipt vouchers can be approved");
        }
        voucher.setStatus(VoucherStatus.APPROVED);
        voucher.setApprovedAt(LocalDateTime.now());
        voucher.setApprovedBy(actor);
        receiptVoucherRepository.save(voucher);
        return postReceiptVoucher(voucherId, actor);
    }

    @Transactional
    public ReceiptVoucherDisplayDto postReceiptVoucher(Long voucherId, String actor) {
        ReceiptVoucher voucher = loadVoucher(voucherId);
        if (voucher.getJournalEntry() != null) {
            return receiptVoucherMapper.toDisplay(voucher);
        }
        if (voucher.getStatus() == VoucherStatus.CANCELLED) {
            throw new BusinessException("Cancelled receipt voucher cannot be recorded");
        }

        boolean requiresApproval = accountingSettingsService.getBooleanSetting("REQUIRE_APPROVAL_FOR_RECEIPTS", true);
        if (requiresApproval && voucher.getStatus() != VoucherStatus.APPROVED) {
            throw new BusinessException("Receipt voucher must be approved before recording");
        }

        Account offsetAccount = resolvePostingOffsetAccount(voucher);
        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                voucher.getVoucherDate(),
                voucher.getDescription(),
                "RECEIPT_VOUCHER",
                voucher.getId(),
                actor,
                List.of(
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(voucher.getCashAccount().getId())
                                .description("Receipt voucher debit")
                                .debit(voucher.getAmount())
                                .credit(BigDecimal.ZERO)
                                .build(),
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(offsetAccount.getId())
                                .description(voucher.getInvoiceReference() != null && !voucher.getInvoiceReference().isBlank()
                                        ? "Receipt voucher credit - invoice settlement"
                                        : "Receipt voucher credit - revenue")
                                .debit(BigDecimal.ZERO)
                                .credit(voucher.getAmount())
                                .build()
                )
        );

        voucher.setJournalEntry(journalEntry);
        if (voucher.getApprovedAt() == null) {
            voucher.setApprovedAt(LocalDateTime.now());
            voucher.setApprovedBy(actor);
        }
        voucher.setStatus(VoucherStatus.APPROVED);
        voucher.setPostedAt(LocalDateTime.now());
        voucher.setPostedBy(actor);
        ReceiptVoucher savedVoucher = receiptVoucherRepository.save(voucher);
        refreshLinkedInvoice(voucher.getInvoiceReference());
        return receiptVoucherMapper.toDisplay(savedVoucher);
    }

    @Transactional
    public ReceiptVoucherDisplayDto cancelReceiptVoucher(Long voucherId, String actor, String reason) {
        ReceiptVoucher voucher = loadVoucher(voucherId);
        if (voucher.getStatus() == VoucherStatus.CANCELLED) {
            throw new BusinessException("Receipt voucher is already cancelled");
        }
        if (voucher.getJournalEntry() != null) {
            JournalEntry reversal = accountingPostingService.reverseJournal(voucher.getJournalEntry(), actor, reason, LocalDate.now());
            voucher.setReversalJournalEntry(reversal);
        }
        voucher.setStatus(VoucherStatus.CANCELLED);
        ReceiptVoucher savedVoucher = receiptVoucherRepository.save(voucher);
        refreshLinkedInvoice(voucher.getInvoiceReference());
        return receiptVoucherMapper.toDisplay(savedVoucher);
    }

    private void applyForm(ReceiptVoucher voucher, ReceiptVoucherFormDto request) {
        Account settlementAccount = resolveSettlementAccount(request.getCashAccountId());
        Account offsetAccount = resolveOffsetAccount(request.getRevenueAccountId());
        if (settlementAccount.getId().equals(offsetAccount.getId())) {
            throw new BusinessException("Settlement and offset accounts must be different");
        }

        voucher.setVoucherDate(request.getVoucherDate());
        voucher.setDescription(normalizeOptional(request.getDescription()));
        voucher.setAmount(normalizeAmount(request.getAmount()));
        voucher.setCashAccount(settlementAccount);
        voucher.setRevenueAccount(offsetAccount);
        voucher.setPaymentMethod(request.getPaymentMethod());
        voucher.setCurrencyCode(request.getCurrencyCode().trim().toUpperCase());
        voucher.setVoucherType(request.getVoucherType().trim().toUpperCase());
        voucher.setPartyName(normalizeOptional(request.getPartyName()));
        voucher.setInvoiceReference(normalizeOptional(request.getInvoiceReference()));
    }

    private Account resolveSettlementAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        if (!account.isActive()) {
            throw new BusinessException("Settlement account must be active");
        }
        if (account.getAccountType() != AccountingType.ASSET) {
            throw new BusinessException("Settlement account must be an asset account");
        }
        return account;
    }

    private Account resolveOffsetAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        if (!account.isActive()) {
            throw new BusinessException("Offset account must be active");
        }
        if (account.getAccountType() != AccountingType.REVENUE) {
            throw new BusinessException("Offset account must be a revenue account");
        }
        return account;
    }

    private Account resolvePostingOffsetAccount(ReceiptVoucher voucher) {
        String invoiceReference = voucher.getInvoiceReference();
        if (invoiceReference == null || invoiceReference.isBlank()) {
            return voucher.getRevenueAccount();
        }

        CustomerInvoice invoice = customerInvoiceRepository.findByInvoiceNumber(invoiceReference.trim());
        if (invoice == null) {
            return voucher.getRevenueAccount();
        }

        if (invoice.getReceivableAccount() == null) {
            throw new BusinessException("Invoice receivable account is required before posting receipt against invoice");
        }

        return invoice.getReceivableAccount();
    }

    private void refreshLinkedInvoice(String invoiceReference) {
        if (invoiceReference == null || invoiceReference.isBlank()) {
            return;
        }
        customerInvoiceService.refreshInvoicePaymentStatus(invoiceReference.trim());
    }

    private void ensureEditable(ReceiptVoucher voucher) {
        if (voucher.getJournalEntry() != null || voucher.getStatus() == VoucherStatus.CANCELLED) {
            throw new BusinessException("Only draft or approved receipt vouchers without a journal entry can be edited");
        }
    }

    private String resolveReference(String reference) {
        String normalized = normalizeOptional(reference);
        if (normalized != null) {
            if (receiptVoucherRepository.existsByReferenceIgnoreCase(normalized)) {
                throw new BusinessException("Receipt voucher reference already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("RECEIPT_VOUCHER");
        } catch (Exception exception) {
            return "RV-" + System.currentTimeMillis();
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private ReceiptVoucher loadVoucher(Long voucherId) {
        return receiptVoucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("ReceiptVoucher", voucherId));
    }
}
