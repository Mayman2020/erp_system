package com.erp.system.common.service;

import com.erp.system.common.dto.LookupItemDto;
import com.erp.system.common.repository.LookupValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LookupService {

    private static final Map<String, String> TYPE_ALIASES = Map.ofEntries(
            Map.entry("account-type", "account-types"),
            Map.entry("account-types", "account-types"),
            Map.entry("status", "statuses"),
            Map.entry("statuses", "statuses"),
            Map.entry("voucher-status", "voucher-statuses"),
            Map.entry("voucher-statuses", "voucher-statuses"),
            Map.entry("journal-entry-status", "journal-entry-statuses"),
            Map.entry("journal-entry-statuses", "journal-entry-statuses"),
            Map.entry("entry-type", "entry-types"),
            Map.entry("entry-types", "entry-types"),
            Map.entry("payment-method", "payment-methods"),
            Map.entry("payment-methods", "payment-methods"),
            Map.entry("receipt-methods", "payment-methods"),
            Map.entry("currency", "currencies"),
            Map.entry("currencies", "currencies"),
            Map.entry("reconciliation-status", "reconciliation-statuses"),
            Map.entry("reconciliation-statuses", "reconciliation-statuses"),
            Map.entry("reconciliation-line-status", "reconciliation-line-statuses"),
            Map.entry("reconciliation-line-statuses", "reconciliation-line-statuses"),
            Map.entry("report-period", "report-periods"),
            Map.entry("report-periods", "report-periods"),
            Map.entry("accounting-method", "accounting-methods"),
            Map.entry("accounting-methods", "accounting-methods"),
            Map.entry("transfer-status", "transfer-statuses"),
            Map.entry("transfer-statuses", "transfer-statuses"),
            Map.entry("transaction-type", "transaction-types"),
            Map.entry("transaction-types", "transaction-types"),
            Map.entry("transaction-status", "transaction-statuses"),
            Map.entry("transaction-statuses", "transaction-statuses"),
            Map.entry("bill-status", "bill-statuses"),
            Map.entry("bill-statuses", "bill-statuses"),
            Map.entry("budget-status", "budget-statuses"),
            Map.entry("budget-statuses", "budget-statuses"),
            Map.entry("check-type", "check-types"),
            Map.entry("check-types", "check-types"),
            Map.entry("check-status", "check-statuses"),
            Map.entry("check-statuses", "check-statuses")
    );

    private final LookupValueRepository lookupValueRepository;

    public List<LookupItemDto> getLookups(String type) {
        String canonicalType = canonicalType(type);
        return lookupValueRepository
                .findByTypeCodeIgnoreCaseAndActiveTrueOrderBySortOrderAscCodeAsc(canonicalType)
                .stream()
                .map(value -> LookupItemDto.builder()
                        .id(value.getId())
                        .typeCode(value.getTypeCode())
                        .code(value.getCode())
                        .nameEn(value.getNameEn())
                        .nameAr(value.getNameAr())
                        .sortOrder(value.getSortOrder())
                        .active(value.isActive())
                        .icon(resolveLookupIcon(canonicalType, value.getCode()))
                        .build())
                .toList();
    }

    private String canonicalType(String type) {
        String normalized = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        return TYPE_ALIASES.getOrDefault(normalized, normalized);
    }

    private String resolveLookupIcon(String type, String code) {
        String normalizedCode = code == null ? "" : code.toUpperCase(Locale.ROOT);
        String normalizedType = type == null ? "" : type.toLowerCase(Locale.ROOT);
        if (normalizedType.contains("payment-method")) {
            return switch (normalizedCode) {
                case "CASH" -> "feather icon-dollar-sign";
                case "BANK" -> "feather icon-credit-card";
                case "CHECK", "CHEQUE" -> "feather icon-file-text";
                default -> "feather icon-circle";
            };
        }
        if (normalizedType.contains("currency")) {
            return "feather icon-globe";
        }
        return switch (normalizedCode) {
            case "ACTIVE", "APPROVED", "POSTED", "MATCHED" -> "feather icon-check-circle";
            case "INACTIVE", "CANCELLED", "REVERSED" -> "feather icon-x-circle";
            case "DRAFT", "OPEN", "UNMATCHED", "UNDER_REVIEW" -> "feather icon-clock";
            default -> "feather icon-circle";
        };
    }
}
