package com.erp.system.accounting.domain;

import com.erp.system.common.enums.AccountingType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    @Test
    void signedOpeningBalanceUsesDebitAsPositive() {
        Account account = new Account();
        account.setAccountType(AccountingType.ASSET);
        account.setOpeningBalance(new BigDecimal("1500.00"));
        account.setOpeningBalanceSide(Account.BalanceSide.DEBIT);

        assertThat(account.signedOpeningBalance()).isEqualByComparingTo("1500.00");
    }

    @Test
    void signedOpeningBalanceUsesCreditAsNegative() {
        Account account = new Account();
        account.setAccountType(AccountingType.EQUITY);
        account.setOpeningBalance(new BigDecimal("1500.00"));
        account.setOpeningBalanceSide(Account.BalanceSide.CREDIT);

        assertThat(account.signedOpeningBalance()).isEqualByComparingTo("-1500.00");
    }

    @Test
    void signedOpeningBalanceFallsBackToNormalBalanceSide() {
        Account account = new Account();
        account.setAccountType(AccountingType.LIABILITY);
        account.setOpeningBalance(new BigDecimal("800.00"));

        assertThat(account.signedOpeningBalance()).isEqualByComparingTo("-800.00");
    }

    @Test
    void synchronizeDerivedFieldsPopulatesLegacyNameAndFullPath() {
        Account parent = new Account();
        parent.setNameEn("Assets");
        ReflectionTestUtils.invokeMethod(parent, "synchronizeDerivedFields");

        Account child = new Account();
        child.setParent(parent);
        child.setNameEn(" Cash on Hand ");

        ReflectionTestUtils.invokeMethod(child, "synchronizeDerivedFields");

        assertThat(child.getNameEn()).isEqualTo("Cash on Hand");
        assertThat(child.getLegacyName()).isEqualTo("Cash on Hand");
        assertThat(child.getFullPath()).isEqualTo("Assets/Cash on Hand");
    }
}
