package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.Bill;
import com.erp.system.accounting.domain.BillLine;
import com.erp.system.accounting.domain.JournalEntry;
import com.erp.system.accounting.dto.display.BillDisplayDto;
import com.erp.system.accounting.dto.display.BillLineDisplayDto;
import com.erp.system.accounting.dto.form.BillFormDto;
import com.erp.system.accounting.dto.form.BillLineFormDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.BillRepository;
import com.erp.system.accounting.support.JournalPostingNarratives;
import com.erp.system.accounting.repository.PaymentVoucherRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.enums.BillStatus;
import com.erp.system.common.enums.VoucherStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
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
public class BillService {

    private final BillRepository billRepository;
    private final AccountRepository accountRepository;
    private final PaymentVoucherRepository paymentVoucherRepository;
    private final NumberingService numberingService;
    private final AccountingPostingService accountingPostingService;

    @Transactional(readOnly = true)
    public List<BillDisplayDto> getBills(BillStatus status, String search) {
        List<Bill> bills = status == null
                ? billRepository.findAllByOrderByBillDateDescIdDesc()
                : billRepository.findByStatusOrderByBillDateDescIdDesc(status);
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return bills.stream()
                .filter(bill -> normalizedSearch == null
                        || bill.getBillNumber().toLowerCase().contains(normalizedSearch)
                        || (bill.getSupplierName() != null && bill.getSupplierName().toLowerCase().contains(normalizedSearch))
                        || (bill.getDescription() != null && bill.getDescription().toLowerCase().contains(normalizedSearch)))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public BillDisplayDto getBill(Long id) {
        return toDisplay(loadBill(id));
    }

    @Transactional
    public BillDisplayDto createBill(BillFormDto request) {
        Bill bill = Bill.builder()
                .billNumber(resolveBillNumber(request.getBillNumber()))
                .status(BillStatus.DRAFT)
                .lines(new ArrayList<>())
                .build();
        applyForm(bill, request);
        return toDisplay(billRepository.save(bill));
    }

    @Transactional
    public BillDisplayDto updateBill(Long id, BillFormDto request) {
        Bill bill = loadBill(id);
        if (bill.getStatus() != BillStatus.DRAFT) {
            throw new BusinessException("Only draft bills can be edited");
        }
        applyForm(bill, request);
        return toDisplay(billRepository.save(bill));
    }

    @Transactional
    public BillDisplayDto approveBill(Long id, String actor) {
        Bill bill = loadBill(id);
        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new BusinessException("Cancelled bills cannot be approved");
        }
        if (bill.getJournalEntry() != null) {
            return toDisplay(bill);
        }
        if (bill.getStatus() != BillStatus.DRAFT && bill.getStatus() != BillStatus.APPROVED) {
            throw new BusinessException("Only draft or approved bills can be approved");
        }
        if (bill.getStatus() == BillStatus.DRAFT) {
            bill.setApprovedAt(LocalDateTime.now());
            bill.setApprovedBy(actor);
        }

        String entryNarrative = JournalPostingNarratives.entryHeader(
                bill.getDescription(),
                JournalPostingNarratives.PURCHASE_INVOICE,
                bill.getBillNumber());
        List<AccountingPostingService.JournalLineDraft> journalLines = new ArrayList<>();
        for (BillLine line : bill.getLines()) {
            Account lineAccount = line.getAccount();
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(lineAccount.getId())
                    .description(JournalPostingNarratives.lineDescriptionOrFallback(line.getDescription(), entryNarrative, lineAccount, true))
                    .debit(line.getLineTotal())
                    .credit(BigDecimal.ZERO)
                    .build());
        }
        if (bill.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (bill.getTaxAccount() == null) {
                throw new BusinessException("Tax account is required when tax amount is greater than zero");
            }
            Account taxAccount = bill.getTaxAccount();
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(taxAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(entryNarrative, taxAccount, true)
                            + " · ضريبة | Tax")
                    .debit(bill.getTaxAmount())
                    .credit(BigDecimal.ZERO)
                    .build());
        }
        Account payableAccount = bill.getPayableAccount();
        journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                .accountId(payableAccount.getId())
                .description(JournalPostingNarratives.lineWithAccount(entryNarrative, payableAccount, false))
                .debit(BigDecimal.ZERO)
                .credit(bill.getTotalAmount())
                .build());

        JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                bill.getBillDate(),
                entryNarrative,
                "BILL",
                bill.getId(),
                actor,
                journalLines
        );

        bill.setJournalEntry(journalEntry);
        bill.setStatus(BillStatus.POSTED);
        bill.setPostedAt(LocalDateTime.now());
        bill.setPostedBy(actor);
        bill.setPaidAmount(BigDecimal.ZERO);
        bill.setOutstandingAmount(bill.getTotalAmount());
        return toDisplay(billRepository.save(bill));
    }

    @Transactional
    public BillDisplayDto postBill(Long id, String actor) {
        return approveBill(id, actor);
    }

    @Transactional
    public BillDisplayDto cancelBill(Long id, String actor, String reason) {
        Bill bill = loadBill(id);
        if (bill.getStatus() == BillStatus.CANCELLED) {
            throw new BusinessException("Bill is already cancelled");
        }
        if (bill.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Bill with posted payments cannot be cancelled");
        }
        if (bill.getStatus() == BillStatus.POSTED || bill.getStatus() == BillStatus.PARTIALLY_PAID || bill.getStatus() == BillStatus.PAID) {
            JournalEntry reversalEntry = accountingPostingService.reverseJournal(bill.getJournalEntry(), actor, reason, LocalDate.now());
            bill.setCancellationJournalEntry(reversalEntry);
        }
        bill.setStatus(BillStatus.CANCELLED);
        bill.setCancelledAt(LocalDateTime.now());
        bill.setCancelledBy(actor);
        bill.setOutstandingAmount(BigDecimal.ZERO);
        return toDisplay(billRepository.save(bill));
    }

    @Transactional
    public void refreshBillPaymentStatus(Long billId) {
        Bill bill = loadBill(billId);
        BigDecimal paidAmount = paymentVoucherRepository.findByBillIdOrderByVoucherDateAscIdAsc(billId).stream()
                .filter(v -> v.getJournalEntry() != null && v.getStatus() != VoucherStatus.CANCELLED)
                .map(voucher -> voucher.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        bill.setPaidAmount(paidAmount);
        bill.setOutstandingAmount(bill.getTotalAmount().subtract(paidAmount).max(BigDecimal.ZERO));
        if (bill.getStatus() != BillStatus.CANCELLED) {
            if (bill.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
                bill.setStatus(BillStatus.PAID);
            } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                bill.setStatus(BillStatus.PARTIALLY_PAID);
            } else if (bill.getJournalEntry() != null) {
                bill.setStatus(BillStatus.POSTED);
            }
        }
        billRepository.save(bill);
    }

    private void applyForm(Bill bill, BillFormDto request) {
        if (request.getDueDate().isBefore(request.getBillDate())) {
            throw new BusinessException("Due date cannot be before bill date");
        }
        Account payableAccount = resolvePayableAccount(request.getPayableAccountId());
        Account taxAccount = request.getTaxAccountId() == null ? null : resolveTaxAccount(request.getTaxAccountId());

        bill.setBillDate(request.getBillDate());
        bill.setDueDate(request.getDueDate());
        bill.setSupplierName(normalizeOptional(request.getSupplierName()));
        bill.setSupplierReference(normalizeOptional(request.getSupplierReference()));
        bill.setDescription(normalizeOptional(request.getDescription()));
        bill.setPayableAccount(payableAccount);
        bill.setTaxAccount(taxAccount);
        bill.setTaxAmount(normalizeAmount(request.getTaxAmount()));

        bill.getLines().clear();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (BillLineFormDto lineRequest : request.getLines()) {
            Account lineAccount = resolveExpenseAccount(lineRequest.getAccountId());
            BigDecimal quantity = normalizePositive(lineRequest.getQuantity(), "Quantity");
            BigDecimal unitPrice = normalizeAmount(lineRequest.getUnitPrice());
            BigDecimal lineTotal = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);

            BillLine line = BillLine.builder()
                    .bill(bill)
                    .account(lineAccount)
                    .description(normalizeOptional(lineRequest.getDescription()))
                    .quantity(quantity)
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build();
            bill.getLines().add(line);
            subtotal = subtotal.add(lineTotal);
        }

        bill.setSubtotal(subtotal);
        bill.setTotalAmount(subtotal.add(bill.getTaxAmount()).setScale(2, RoundingMode.HALF_UP));
        bill.setOutstandingAmount(bill.getTotalAmount().subtract(bill.getPaidAmount()).max(BigDecimal.ZERO));
    }

    private Account resolvePayableAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        if (!account.isActive() || account.getAccountType() != AccountingType.LIABILITY) {
            throw new BusinessException("Payable account must be an active liability account");
        }
        return account;
    }

    private Account resolveTaxAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        if (!account.isActive()) {
            throw new BusinessException("Tax account must be active");
        }
        return account;
    }

    private Account resolveExpenseAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        if (!account.isActive()) {
            throw new BusinessException("Bill line account must be active");
        }
        if (account.getAccountType() != AccountingType.EXPENSE && account.getAccountType() != AccountingType.ASSET) {
            throw new BusinessException("Bill line account must be an expense or inventory asset account");
        }
        return account;
    }

    private String resolveBillNumber(String billNumber) {
        String normalized = normalizeOptional(billNumber);
        if (normalized != null) {
            if (billRepository.existsByBillNumberIgnoreCase(normalized)) {
                throw new BusinessException("Bill number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("BILL_NUMBER");
        } catch (Exception exception) {
            return "BILL-" + System.currentTimeMillis();
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Amount cannot be negative");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizePositive(BigDecimal amount, String label) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(label + " must be greater than zero");
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

    private Bill loadBill(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", id));
    }

    private BillDisplayDto toDisplay(Bill bill) {
        return BillDisplayDto.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .billDate(bill.getBillDate())
                .dueDate(bill.getDueDate())
                .supplierName(bill.getSupplierName())
                .supplierReference(bill.getSupplierReference())
                .description(bill.getDescription())
                .subtotal(bill.getSubtotal())
                .taxAmount(bill.getTaxAmount())
                .totalAmount(bill.getTotalAmount())
                .paidAmount(bill.getPaidAmount())
                .outstandingAmount(bill.getOutstandingAmount())
                .status(bill.getStatus())
                .payableAccountId(bill.getPayableAccount() != null ? bill.getPayableAccount().getId() : null)
                .payableAccountCode(bill.getPayableAccount() != null ? bill.getPayableAccount().getCode() : null)
                .payableAccountName(bill.getPayableAccount() != null ? bill.getPayableAccount().getNameEn() : null)
                .taxAccountId(bill.getTaxAccount() != null ? bill.getTaxAccount().getId() : null)
                .taxAccountCode(bill.getTaxAccount() != null ? bill.getTaxAccount().getCode() : null)
                .taxAccountName(bill.getTaxAccount() != null ? bill.getTaxAccount().getNameEn() : null)
                .journalEntryId(bill.getJournalEntry() != null ? bill.getJournalEntry().getId() : null)
                .cancellationJournalEntryId(bill.getCancellationJournalEntry() != null ? bill.getCancellationJournalEntry().getId() : null)
                .approvedAt(bill.getApprovedAt())
                .approvedBy(bill.getApprovedBy())
                .postedAt(bill.getPostedAt())
                .postedBy(bill.getPostedBy())
                .lines(bill.getLines().stream().map(line -> BillLineDisplayDto.builder()
                        .id(line.getId())
                        .accountId(line.getAccount().getId())
                        .accountCode(line.getAccount().getCode())
                        .accountName(line.getAccount().getNameEn())
                        .description(line.getDescription())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .lineTotal(line.getLineTotal())
                        .build()).toList())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }
}
