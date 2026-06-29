package com.erp.system.accounting.support;

import com.erp.system.accounting.domain.Account;
import com.erp.system.accounting.repository.AccountRepository;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostingAccountResolver {

    private final AccountRepository accountRepository;

    public Account requireActive(String label, String... codes) {
        for (String code : codes) {
            Account account = accountRepository.findByCode(code)
                    .filter(Account::isActive)
                    .orElse(null);
            if (account != null) {
                return account;
            }
        }
        throw new BusinessException("Active " + label + " account is required (" + String.join(" or ", codes) + ")");
    }

    public Account receivable(Account configuredReceivable) {
        if (configuredReceivable != null
                && configuredReceivable.isActive()
                && isReceivableAccount(configuredReceivable)) {
            return configuredReceivable;
        }
        return requireActive("Accounts Receivable", "1200", "1210");
    }

    public Account salesRevenue() {
        return requireActive("Revenue", "4100", "4110");
    }

    public Account serviceRevenue() {
        return requireActive("Service Revenue", "4200", "4120");
    }

    public Account taxPayable() {
        return requireActive("Tax Payable", "2210", "2200");
    }

    public Account inventory() {
        return requireActive("Inventory", "1300", "1310");
    }

    public Account cogs() {
        return requireActive("Cost of Goods Sold", "5130", "5100");
    }

    public Account accountsPayable() {
        return requireActive("Accounts Payable", "2110", "2100");
    }

    private boolean isReceivableAccount(Account account) {
        if (account.getAccountType() != AccountingType.ASSET) {
            return false;
        }
        String code = account.getCode() == null ? "" : account.getCode();
        return code.startsWith("12");
    }
}
