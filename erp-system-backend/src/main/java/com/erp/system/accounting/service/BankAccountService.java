package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.domain.BankAccount;
import com.erp.system.accounting.dto.display.BankAccountDisplayDto;
import com.erp.system.accounting.dto.form.BankAccountFormDto;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.BankAccountRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryLineRepository journalEntryLineRepository;

    @Transactional(readOnly = true)
    public List<BankAccountDisplayDto> getBankAccounts(Boolean active, String search) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        List<BankAccount> bankAccounts = active == null
                ? bankAccountRepository.findAllByOrderByBankNameAscAccountNumberAsc()
                : active
                ? bankAccountRepository.findByActiveTrueOrderByBankNameAscAccountNumberAsc()
                : bankAccountRepository.findAllByOrderByBankNameAscAccountNumberAsc().stream().filter(account -> !account.isActive()).toList();

        return bankAccounts.stream()
                .filter(account -> normalizedSearch == null
                        || account.getBankName().toLowerCase().contains(normalizedSearch)
                        || account.getAccountNumber().toLowerCase().contains(normalizedSearch)
                        || (account.getIban() != null && account.getIban().toLowerCase().contains(normalizedSearch)))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public BankAccountDisplayDto getBankAccount(Long id) {
        return toDisplay(loadBankAccount(id));
    }

    @Transactional
    public BankAccountDisplayDto createBankAccount(BankAccountFormDto request) {
        if (bankAccountRepository.existsByAccountNumberIgnoreCase(request.getAccountNumber().trim())) {
            throw new BusinessException("Bank account number already exists");
        }
        BankAccount bankAccount = new BankAccount();
        applyForm(bankAccount, request);
        return toDisplay(bankAccountRepository.save(bankAccount));
    }

    @Transactional
    public BankAccountDisplayDto updateBankAccount(Long id, BankAccountFormDto request) {
        BankAccount bankAccount = loadBankAccount(id);
        if (!bankAccount.getAccountNumber().equalsIgnoreCase(request.getAccountNumber().trim())
                && bankAccountRepository.existsByAccountNumberIgnoreCase(request.getAccountNumber().trim())) {
            throw new BusinessException("Bank account number already exists");
        }
        applyForm(bankAccount, request);
        return toDisplay(bankAccountRepository.save(bankAccount));
    }

    @Transactional
    public void deleteBankAccount(Long id) {
        BankAccount bankAccount = loadBankAccount(id);
        BigDecimal movement = journalEntryLineRepository.sumNetMovementByAccountId(bankAccount.getLinkedAccount().getId());
        if (movement.compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("Used bank accounts cannot be deleted");
        }
        bankAccountRepository.delete(bankAccount);
    }

    private void applyForm(BankAccount bankAccount, BankAccountFormDto request) {
        Account linkedAccount = accountRepository.findById(request.getLinkedAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.getLinkedAccountId()));
        if (!linkedAccount.isActive() || !linkedAccount.isPostable() || linkedAccount.getAccountType() != AccountingType.ASSET) {
            throw new BusinessException("Linked bank account must be an active asset account");
        }

        bankAccount.setBankName(request.getBankName().trim());
        bankAccount.setAccountNumber(request.getAccountNumber().trim());
        bankAccount.setIban(request.getIban() == null ? null : request.getIban().trim());
        bankAccount.setCurrency(request.getCurrency().trim());
        bankAccount.setOpeningBalance(request.getOpeningBalance());
        bankAccount.setLinkedAccount(linkedAccount);
        bankAccount.setActive(Boolean.TRUE.equals(request.getActive()));
        bankAccount.setCurrentBalance(calculateCurrentBalance(linkedAccount.getId(), request.getOpeningBalance()));
    }

    private BigDecimal calculateCurrentBalance(Long linkedAccountId, BigDecimal openingBalance) {
        BigDecimal movement = journalEntryLineRepository.sumNetMovementByAccountId(linkedAccountId);
        return (openingBalance == null ? BigDecimal.ZERO : openingBalance).add(movement);
    }

    private BankAccount loadBankAccount(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", id));
    }

    private BankAccountDisplayDto toDisplay(BankAccount bankAccount) {
        BigDecimal currentBalance = calculateCurrentBalance(bankAccount.getLinkedAccount().getId(), bankAccount.getOpeningBalance());
        return BankAccountDisplayDto.builder()
                .id(bankAccount.getId())
                .bankName(bankAccount.getBankName())
                .accountNumber(bankAccount.getAccountNumber())
                .iban(bankAccount.getIban())
                .currency(bankAccount.getCurrency())
                .openingBalance(bankAccount.getOpeningBalance())
                .currentBalance(currentBalance)
                .active(bankAccount.isActive())
                .linkedAccountId(bankAccount.getLinkedAccount().getId())
                .linkedAccountCode(bankAccount.getLinkedAccount().getCode())
                .linkedAccountName(bankAccount.getLinkedAccount().getNameEn())
                .createdAt(bankAccount.getCreatedAt())
                .updatedAt(bankAccount.getUpdatedAt())
                .build();
    }
}
