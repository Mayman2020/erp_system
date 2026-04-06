package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.Bill;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.domain.PaymentVoucher;
import com.erp.system.accounting.dto.display.PaymentVoucherDisplayDto;
import com.erp.system.accounting.dto.form.PaymentVoucherFormDto;
import com.erp.system.accounting.mapper.PaymentVoucherMapper;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.BillRepository;
import com.erp.system.accounting.repository.PaymentVoucherRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.enums.BillStatus;
import com.erp.system.common.enums.PaymentMethod;
import com.erp.system.common.enums.VoucherStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.accounting.support.JournalPostingNarratives;
import com.erp.system.common.service.AccountingSettingsService;
import com.erp.system.common.service.NumberingService;
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
public class PaymentVoucherService {

    private final PaymentVoucherRepository paymentVoucherRepository;
    private final AccountRepository accountRepository;
    private final BillRepository billRepository;
    private final PaymentVoucherMapper paymentVoucherMapper;
    private final NumberingService numberingService;
    private final AccountingSettingsService accountingSettingsService;
    private final AccountingPostingService accountingPostingService;

    @Transactional(readOnly = true)
    public List<PaymentVoucherDisplayDto> getPaymentVouchers(VoucherStatus status,
                                                             PaymentMethod paymentMethod,
                                                             Long bankAccountId,
                                                             String beneficiary,
                                                             BigDecimal minAmount,
                                                             BigDecimal maxAmount,
                                                             LocalDate fromDate,
                                                             LocalDate toDate,
                                                             String search) {
        List<PaymentVoucher> vouchers = status == null
                ? paymentVoucherRepository.findAllByOrderByVoucherDateDescIdDesc()
                : paymentVoucherRepository.findByStatusOrderByVoucherDateDescIdDesc(status);

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return vouchers.stream()
                .filter(voucher -> fromDate == null || !voucher.getVoucherDate().isBefore(fromDate))
                .filter(voucher -> toDate == null || !voucher.getVoucherDate().isAfter(toDate))
                .filter(voucher -> paymentMethod == null || voucher.getPaymentMethod() == paymentMethod)
                .filter(voucher -> bankAccountId == null || voucher.getCashAccount().getId().equals(bankAccountId))
                .filter(voucher -> beneficiary == null || beneficiary.isBlank()
                        || (voucher.getPartyName() != null && voucher.getPartyName().toLowerCase().contains(beneficiary.trim().toLowerCase())))
                .filter(voucher -> minAmount == null || voucher.getAmount().compareTo(minAmount) >= 0)
                .filter(voucher -> maxAmount == null || voucher.getAmount().compareTo(maxAmount) <= 0)
                .filter(voucher -> normalizedSearch == null
                        || voucher.getReference().toLowerCase().contains(normalizedSearch)
                        || (voucher.getDescription() != null && voucher.getDescription().toLowerCase().contains(normalizedSearch))
                        || (voucher.getPartyName() != null && voucher.getPartyName().toLowerCase().contains(normalizedSearch)))
                .map(paymentVoucherMapper::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaymentVoucherDisplayDto getPaymentVoucher(Long voucherId) {
        return paymentVoucherMapper.toDisplay(loadVoucher(voucherId));
    }

    @Transactional
    public PaymentVoucherDisplayDto createPaymentVoucher(PaymentVoucherFormDto request) {
        PaymentVoucher voucher = PaymentVoucher.builder()
                .reference(resolveReference(request.getReference()))
                .status(VoucherStatus.DRAFT)
                .build();
        applyForm(voucher, request);
        return paymentVoucherMapper.toDisplay(paymentVoucherRepository.save(voucher));
    }

    @Transactional
    public PaymentVoucherDisplayDto updatePaymentVoucher(Long voucherId, PaymentVoucherFormDto request) {
        PaymentVoucher voucher = loadVoucher(voucherId);
        ensureEditable(voucher);
        applyForm(voucher, request);
        return paymentVoucherMapper.toDisplay(paymentVoucherRepository.save(voucher));
    }

    @Transactional
    public PaymentVoucherDisplayDto approvePaymentVoucher(Long voucherId, String actor) {
        PaymentVoucher voucher = loadVoucher(voucherId);
        if (voucher.getStatus() != VoucherStatus.DRAFT) {
            throw new BusinessException("Only draft payment vouchers can be approved");
        }
        voucher.setStatus(VoucherStatus.APPROVED);
        voucher.setApprovedAt(LocalDateTime.now());
        voucher.setApprovedBy(actor);
        paymentVoucherRepository.save(voucher);
        return postPaymentVoucher(voucherId, actor);
    }

    @Transactional
    public PaymentVoucherDisplayDto postPaymentVoucher(Long voucherId, String actor) {
        PaymentVoucher voucher = loadVoucher(voucherId);
        if (voucher.getJournalEntry() != null) {
            return paymentVoucherMapper.toDisplay(voucher);
        }
        if (voucher.getStatus() == VoucherStatus.CANCELLED) {
            throw new BusinessException("Cancelled payment voucher cannot be recorded");
        }

        boolean requiresApproval = accountingSettingsService.getBooleanSetting("REQUIRE_APPROVAL_FOR_PAYMENTS", true);
        if (requiresApproval && voucher.getStatus() != VoucherStatus.APPROVED) {
            throw new BusinessException("Payment voucher must be approved before recording");
        }

        Bill linkedBill = resolveBill(voucher.getBillId());
        if (linkedBill != null) {
            if (linkedBill.getStatus() != BillStatus.POSTED && linkedBill.getStatus() != BillStatus.PARTIALLY_PAID) {
                throw new BusinessException("Only posted bills can receive payments");
            }
            if (linkedBill.getOutstandingAmount().compareTo(voucher.getAmount()) < 0) {
                throw new BusinessException("Payment amount exceeds bill outstanding amount");
            }
        }

        String entryNarrative = JournalPostingNarratives.entryHeader(
                voucher.getDescription(),
                JournalPostingNarratives.PAYMENT_BOND,
                voucher.getReference());
        Account expenseAccount = voucher.getExpenseAccount();
        Account cashAccount = voucher.getCashAccount();
        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                voucher.getVoucherDate(),
                entryNarrative,
                "PAYMENT_VOUCHER",
                voucher.getId(),
                actor,
                List.of(
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(expenseAccount.getId())
                                .description(JournalPostingNarratives.lineWithAccount(entryNarrative, expenseAccount, true))
                                .debit(voucher.getAmount())
                                .credit(BigDecimal.ZERO)
                                .build(),
                        AccountingPostingService.JournalLineDraft.builder()
                                .accountId(cashAccount.getId())
                                .description(JournalPostingNarratives.lineWithAccount(entryNarrative, cashAccount, false))
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
        PaymentVoucher savedVoucher = paymentVoucherRepository.save(voucher);
        if (linkedBill != null) {
            recalculateBillBalances(linkedBill);
        }
        return paymentVoucherMapper.toDisplay(savedVoucher);
    }

    @Transactional
    public PaymentVoucherDisplayDto cancelPaymentVoucher(Long voucherId, String actor, String reason) {
        PaymentVoucher voucher = loadVoucher(voucherId);
        if (voucher.getStatus() == VoucherStatus.CANCELLED) {
            throw new BusinessException("Payment voucher is already cancelled");
        }

        Bill linkedBill = resolveBill(voucher.getBillId());
        if (voucher.getJournalEntry() != null) {
            JournalEntry reversal = accountingPostingService.reverseJournal(
                    voucher.getJournalEntry(),
                    actor,
                    reason,
                    LocalDate.now()
            );
            voucher.setReversalJournalEntry(reversal);
        }

        voucher.setStatus(VoucherStatus.CANCELLED);
        PaymentVoucher savedVoucher = paymentVoucherRepository.save(voucher);
        if (linkedBill != null) {
            recalculateBillBalances(linkedBill);
        }
        return paymentVoucherMapper.toDisplay(savedVoucher);
    }

    private void applyForm(PaymentVoucher voucher, PaymentVoucherFormDto request) {
        Account cashAccount = resolveAccount(request.getCashAccountId(), AccountingType.ASSET, "Settlement account");
        Account offsetAccount = resolveOffsetAccount(request.getExpenseAccountId());
        if (cashAccount.getId().equals(offsetAccount.getId())) {
            throw new BusinessException("Settlement and offset accounts must be different");
        }

        Bill linkedBill = resolveBill(request.getBillId());
        if (linkedBill != null && linkedBill.getPayableAccount() != null) {
            offsetAccount = linkedBill.getPayableAccount();
        }

        voucher.setVoucherDate(request.getVoucherDate());
        voucher.setDescription(normalizeOptional(request.getDescription()));
        voucher.setAmount(normalizeAmount(request.getAmount()));
        voucher.setCashAccount(cashAccount);
        voucher.setExpenseAccount(offsetAccount);
        voucher.setPaymentMethod(request.getPaymentMethod());
        voucher.setCurrencyCode(request.getCurrencyCode().trim().toUpperCase());
        voucher.setVoucherType(request.getVoucherType().trim().toUpperCase());
        voucher.setBillId(request.getBillId());
        voucher.setPartyName(normalizeOptional(request.getPartyName()));
        voucher.setLinkedDocumentReference(normalizeOptional(request.getLinkedDocumentReference()));
    }

    private Account resolveAccount(Long accountId, AccountingType expectedType, String label) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        if (!account.isActive()) {
            throw new BusinessException(label + " must be active");
        }
        if (account.getAccountType() != expectedType) {
            throw new BusinessException(label + " must be of type " + expectedType);
        }
        return account;
    }

    private Account resolveOffsetAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        if (!account.isActive()) {
            throw new BusinessException("Offset account must be active");
        }
        if (account.getAccountType() != AccountingType.EXPENSE && account.getAccountType() != AccountingType.LIABILITY) {
            throw new BusinessException("Offset account must be an expense or payable account");
        }
        return account;
    }

    private Bill resolveBill(Long billId) {
        if (billId == null) {
            return null;
        }
        return billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", billId));
    }

    private void recalculateBillBalances(Bill bill) {
        BigDecimal paidAmount = paymentVoucherRepository.findByBillIdOrderByVoucherDateAscIdAsc(bill.getId()).stream()
                .filter(v -> v.getJournalEntry() != null && v.getStatus() != VoucherStatus.CANCELLED)
                .map(PaymentVoucher::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        bill.setPaidAmount(paidAmount);
        bill.setOutstandingAmount(bill.getTotalAmount().subtract(paidAmount).max(BigDecimal.ZERO));
        if (bill.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0 && bill.getStatus() != BillStatus.CANCELLED) {
            bill.setStatus(BillStatus.PAID);
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0 && bill.getStatus() != BillStatus.CANCELLED) {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        } else if (bill.getStatus() != BillStatus.CANCELLED) {
            bill.setStatus(BillStatus.POSTED);
        }
        billRepository.save(bill);
    }

    private void ensureEditable(PaymentVoucher voucher) {
        if (voucher.getJournalEntry() != null || voucher.getStatus() == VoucherStatus.CANCELLED) {
            throw new BusinessException("Only draft or approved payment vouchers without a journal entry can be edited");
        }
    }

    private String resolveReference(String reference) {
        String normalized = normalizeOptional(reference);
        if (normalized != null) {
            if (paymentVoucherRepository.existsByReferenceIgnoreCase(normalized)) {
                throw new BusinessException("Payment voucher reference already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("PAYMENT_VOUCHER");
        } catch (Exception exception) {
            return "PV-" + System.currentTimeMillis();
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

    private PaymentVoucher loadVoucher(Long voucherId) {
        return paymentVoucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentVoucher", voucherId));
    }
}
