package com.erp.system.accounting.support;

import com.erp.system.accounting.domain.Account;
import com.erp.system.common.enums.TransactionType;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds journal entry headers and line narratives for automated postings so that
 * descriptions carry user text when present, otherwise a type default, and lines
 * always reference the posted accounts.
 */
public final class JournalPostingNarratives {

    public static final String PAYMENT_BOND = "سند دفع | Payment bond";
    public static final String COLLECTION_BOND = "سند قبض | Collection bond";
    public static final String SALES_INVOICE = "فاتورة مبيعات | Sales invoice";
    public static final String PURCHASE_INVOICE = "فاتورة مشتريات | Purchase invoice";
    public static final String RECEIVED_CHECK_DEPOSIT = "إيداع شيك مستلم | Received check deposit";
    public static final String ISSUED_CHECK_CLEARANCE = "صرف شيك صادر | Issued check clearance";

    private JournalPostingNarratives() {
    }

    public static String entryHeader(String userDescription, String defaultWhenBlank, String documentReference) {
        if (StringUtils.hasText(userDescription)) {
            return userDescription.trim();
        }
        String ref = normalizeRef(documentReference);
        if (ref != null) {
            return defaultWhenBlank + " — " + ref;
        }
        return defaultWhenBlank;
    }

    public static String transactionTypeDefault(TransactionType type) {
        if (type == null) {
            return "معاملة مالية | Financial transaction";
        }
        return switch (type) {
            case SALE -> "بيع | Sale";
            case PURCHASE -> "شراء | Purchase";
            case REFUND -> "استرجاع | Refund";
            case ADJUSTMENT -> "تسوية | Adjustment";
        };
    }

    /**
     * Line text: base narrative (usually the entry header) plus localized debit/credit hint and account.
     */
    public static String lineWithAccount(String narrativeBase, Account account, boolean debitLine) {
        String side = debitLine ? "مدين | Debit" : "دائن | Credit";
        return narrativeBase + " · " + side + ": " + accountCaption(account);
    }

    /**
     * Prefer the user-entered line description; otherwise fall back to the entry narrative with account side.
     */
    public static String lineDescriptionOrFallback(String lineUserDescription, String narrativeBase, Account account, boolean debitLine) {
        if (StringUtils.hasText(lineUserDescription)) {
            return lineUserDescription.trim() + " · " + accountCaption(account);
        }
        return lineWithAccount(narrativeBase, account, debitLine);
    }

    public static String accountCaption(Account account) {
        String code = account.getCode() == null ? "" : account.getCode().trim();
        String names = joinDistinctNames(account.getNameAr(), account.getNameEn());
        if (!StringUtils.hasText(names) && account.getLegacyName() != null) {
            names = account.getLegacyName().trim();
        }
        if (!StringUtils.hasText(code)) {
            return names;
        }
        if (!StringUtils.hasText(names)) {
            return code;
        }
        return code + " — " + names;
    }

    public static String receiptCreditInvoiceSuffix() {
        return "تسوية فاتورة | Invoice settlement";
    }

    private static String normalizeRef(String documentReference) {
        if (!StringUtils.hasText(documentReference)) {
            return null;
        }
        String t = documentReference.trim();
        return t.isEmpty() ? null : t;
    }

    private static String joinDistinctNames(String ar, String en) {
        Set<String> parts = new LinkedHashSet<>();
        if (StringUtils.hasText(ar)) {
            parts.add(ar.trim());
        }
        if (StringUtils.hasText(en)) {
            parts.add(en.trim());
        }
        return parts.stream().collect(Collectors.joining(" / "));
    }
}
