package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.dto.display.AccountDisplayDto;
import com.erp.system.accounting.dto.display.AccountTreeDisplayDto;
import com.erp.system.accounting.dto.form.AccountFormDto;
import com.erp.system.accounting.mapper.AccountMapper;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.accounting.repository.JournalEntryLineRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final NumberingService numberingService;
    private final JournalEntryLineRepository journalEntryLineRepository;

    @Transactional(readOnly = true)
    public List<AccountDisplayDto> getAccounts() {
        return accountRepository.findAllByOrderByCodeAsc().stream()
                .map(this::toDisplay)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountTreeDisplayDto> getAccountTree() {
        List<Account> accounts = accountRepository.findAllByOrderByCodeAsc();
        Map<Long, AccountTreeDisplayDto> nodesById = new LinkedHashMap<>();

        for (Account account : accounts) {
            nodesById.put(account.getId(), AccountTreeDisplayDto.builder()
                    .id(account.getId())
                    .code(account.getCode())
                    .name(resolveLocalizedName(account))
                    .nameAr(account.getNameAr())
                    .nameEn(account.getNameEn())
                    .accountType(account.getAccountType())
                    .financialStatement(account.getAccountType().financialStatement())
                    .level(account.getLevel())
                    .active(account.isActive())
                    .children(new ArrayList<>())
                    .build());
        }

        List<AccountTreeDisplayDto> roots = new ArrayList<>();
        for (Account account : accounts) {
            AccountTreeDisplayDto node = nodesById.get(account.getId());
            if (account.getParent() == null) {
                roots.add(node);
                continue;
            }

            AccountTreeDisplayDto parentNode = nodesById.get(account.getParent().getId());
            if (parentNode != null) {
                parentNode.getChildren().add(node);
            }
        }

        sortTree(roots);
        return roots;
    }

    @Transactional(readOnly = true)
    public AccountDisplayDto getAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id));
        return toDisplay(account);
    }

    @Transactional(readOnly = true)
    public List<AccountDisplayDto> searchAccounts(String search, AccountingType type, Boolean active) {
        List<Account> accounts;

        if (search != null && !search.trim().isEmpty()) {
            accounts = accountRepository.searchAccounts(search.trim());
        } else {
            accounts = accountRepository.findAllByOrderByCodeAsc();
        }

        return accounts.stream()
                .filter(account -> type == null || account.getAccountType() == type)
                .filter(account -> active == null || account.isActive() == active)
                .map(this::toDisplay)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountDisplayDto createAccount(AccountFormDto request) {
        validateAccountForm(request, null);

        String code = generateAccountCode(request.getCode(), request.getParentId());
        if (accountRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Account code already exists: " + code);
        }

        Account account = new Account();
        applyFormToAccount(account, request, code);
        account = accountRepository.save(account);

        updateChildrenLevels(account);
        return toDisplay(account);
    }

    @Transactional
    public AccountDisplayDto updateAccount(Long accountId, AccountFormDto request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        validateAccountForm(request, accountId);

        String code = request.getCode() != null ? request.getCode().trim() : account.getCode();
        if (!code.equalsIgnoreCase(account.getCode()) && accountRepository.existsByCodeIgnoreCaseAndIdNot(code, accountId)) {
            throw new BusinessException("Account code already exists: " + code);
        }

        applyFormToAccount(account, request, code);
        account = accountRepository.save(account);

        updateChildrenLevels(account);
        return toDisplay(account);
    }

    @Transactional
    public void deactivateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        if (isAccountUsedInJournalEntries(accountId)) {
            throw new BusinessException("Cannot deactivate account that is used in journal entries");
        }

        account.setActive(false);
        accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        if (isAccountUsedInJournalEntries(accountId)) {
            throw new BusinessException("Cannot delete account that is used in journal entries");
        }

        if (account.getChildren() != null && !account.getChildren().isEmpty()) {
            throw new BusinessException("Cannot delete account that has child accounts");
        }

        accountRepository.delete(account);
    }

    @Transactional
    public void activateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        account.setActive(true);
        accountRepository.save(account);
    }

    private void validateAccountForm(AccountFormDto request, Long accountId) {
        if (request.getNameEn() == null || request.getNameEn().trim().isEmpty()) {
            throw new BusinessException("English account name is required");
        }

        if (request.getAccountType() == null) {
            throw new BusinessException("Account type is required");
        }

        // Validate parent
        if (request.getParentId() != null) {
            Account parent = accountRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Parent account not found"));

            if (!parent.isActive()) {
                throw new BusinessException("Parent account must be active");
            }

            if (!parent.getAccountType().financialStatement().equals(request.getAccountType().financialStatement())) {
                throw new BusinessException("Child account must remain within the same financial statement classification as the parent");
            }

            if (parent.getAccountType() != request.getAccountType()) {
                throw new BusinessException("Child account type must match parent account type");
            }

            // Prevent circular reference
            if (accountId != null && isCircularReference(accountId, request.getParentId())) {
                throw new BusinessException("Circular hierarchy not allowed");
            }
        }

        // Validate opening balance side
        if (request.getOpeningBalance() != null && request.getOpeningBalance().compareTo(BigDecimal.ZERO) != 0) {
            if (request.getOpeningBalanceSide() == null) {
                throw new BusinessException("Opening balance side is required when opening balance is not zero");
            }

            boolean isDebitType = request.getAccountType() == AccountingType.ASSET || request.getAccountType() == AccountingType.EXPENSE;
            boolean isDebitSide = request.getOpeningBalanceSide() == Account.BalanceSide.DEBIT;

            if (isDebitType != isDebitSide) {
                throw new BusinessException("Opening balance side must match account type normal balance");
            }
        }
    }

    private String generateAccountCode(String requestedCode, Long parentId) {
        if (requestedCode != null && !requestedCode.trim().isEmpty()) {
            return requestedCode.trim();
        }

        // Auto-generate code
        try {
            return numberingService.generateNextNumber("ACCOUNT_CODE");
        } catch (Exception e) {
            // Fallback to simple increment
            List<Account> accounts = accountRepository.findAllByOrderByCodeAsc();
            int maxCode = accounts.stream()
                    .map(Account::getCode)
                    .filter(code -> code.matches("\\d+"))
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(999);
            return String.valueOf(maxCode + 1);
        }
    }

    private void applyFormToAccount(Account account, AccountFormDto request, String code) {
        Account parent = request.getParentId() != null ?
                accountRepository.findById(request.getParentId()).orElse(null) : null;

        account.setCode(code);
        account.setNameEn(request.getNameEn().trim());
        account.setNameAr(request.getNameAr() != null ? request.getNameAr().trim() : null);
        account.setParent(parent);
        account.setAccountType(request.getAccountType());
        account.setLevel(parent == null ? 1 : parent.getLevel() + 1);
        account.setActive(request.getActive() != null ? request.getActive() : true);
        account.setOpeningBalance(request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO);
        account.setOpeningBalanceSide(request.getOpeningBalanceSide());
    }

    private void updateChildrenLevels(Account parent) {
        updateChildrenLevelsRecursive(parent, parent.getLevel() + 1);
    }

    private void updateChildrenLevelsRecursive(Account parent, int level) {
        for (Account child : parent.getChildren()) {
            child.setLevel(level);
            accountRepository.save(child);
            updateChildrenLevelsRecursive(child, level + 1);
        }
    }

    private boolean isCircularReference(Long accountId, Long parentId) {
        Long currentId = parentId;
        while (currentId != null) {
            if (currentId.equals(accountId)) {
                return true;
            }
            Account current = accountRepository.findById(currentId).orElse(null);
            if (current == null) break;
            currentId = current.getParent() != null ? current.getParent().getId() : null;
        }
        return false;
    }

    private boolean isAccountUsedInJournalEntries(Long accountId) {
        return journalEntryLineRepository.existsByAccountId(accountId);
    }

    private AccountDisplayDto toDisplay(Account account) {
        AccountDisplayDto dto = accountMapper.toDisplay(account);
        dto.setName(resolveLocalizedName(account));
        dto.setNameAr(account.getNameAr());
        dto.setNameEn(account.getNameEn());
        dto.setFinancialStatement(account.getAccountType().financialStatement());
        return dto;
    }

    private String resolveLocalizedName(Account account) {
        String language = LocaleContextHolder.getLocale().getLanguage();
        if ("ar".equalsIgnoreCase(language)) {
            return account.getNameAr() != null ? account.getNameAr() : account.getNameEn();
        }
        return account.getNameEn();
    }

    private void sortTree(List<AccountTreeDisplayDto> nodes) {
        nodes.sort(Comparator.comparing(AccountTreeDisplayDto::getCode));
        for (AccountTreeDisplayDto node : nodes) {
            sortTree(node.getChildren());
        }
    }
}
