package com.erp.system.common.service;

import com.erp.system.common.dto.LookupItemDto;
import com.erp.system.common.entity.LookupValue;
import com.erp.system.common.enums.AccountingType;
import com.erp.system.common.enums.JournalEntryStatus;
import com.erp.system.common.enums.PaymentMethod;
import com.erp.system.common.enums.ReconciliationLineStatus;
import com.erp.system.common.enums.ReconciliationStatus;
import com.erp.system.common.enums.VoucherStatus;
import com.erp.system.common.repository.LookupValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LookupService {
    private final LookupValueRepository lookupValueRepository;

    public List<LookupItemDto> getLookups(String type) {
        String normalized = type.toLowerCase(Locale.ROOT);
        List<LookupItemDto> fromTable = lookupValueRepository
                .findByTypeCodeIgnoreCaseAndActiveTrueOrderBySortOrderAscCodeAsc(normalized)
                .stream()
                .map(value -> LookupItemDto.builder()
                        .code(value.getCode())
                        .icon(resolveLookupIcon(normalized, value.getCode()))
                        .build())
                .toList();
        if (!fromTable.isEmpty()) {
            return fromTable;
        }
        return fallbackLookups(normalized);
    }

    private <T extends Enum<T>> List<LookupItemDto> enumCodes(T[] values) {
        return Arrays.stream(values)
                .map(value -> LookupItemDto.builder()
                        .code(value.name())
                        .icon(resolveLookupIcon(null, value.name()))
                        .build())
                .toList();
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

    private List<LookupItemDto> fallbackLookups(String type) {
        return switch (type) {
            case "account-type", "account-types" -> enumCodes(AccountingType.values());
            case "status", "statuses" -> List.of(
                    LookupItemDto.builder().code("ACTIVE").build(),
                    LookupItemDto.builder().code("INACTIVE").build()
            );
            case "voucher-status", "voucher-statuses" -> enumCodes(VoucherStatus.values());
            case "journal-entry-status", "journal-entry-statuses" -> enumCodes(JournalEntryStatus.values());
            case "entry-type", "entry-types" -> Stream.of("MANUAL", "ADJUSTMENT", "OPENING", "CLOSING", "REVERSAL")
                    .map(code -> LookupItemDto.builder().code(code).build())
                    .toList();
            case "payment-method", "payment-methods", "receipt-methods" -> enumCodes(PaymentMethod.values());
            case "currency", "currencies" -> Stream.of("USD", "EUR", "GBP", "AED", "SAR", "EGP")
                    .map(code -> LookupItemDto.builder().code(code).build())
                    .toList();
            case "reconciliation-status", "reconciliation-statuses" -> enumCodes(ReconciliationStatus.values());
            case "reconciliation-line-status", "reconciliation-line-statuses" -> enumCodes(ReconciliationLineStatus.values());
            case "report-period", "report-periods" -> Stream.of("THIS_MONTH", "LAST_MONTH", "THIS_QUARTER", "THIS_YEAR", "CUSTOM")
                    .map(code -> LookupItemDto.builder().code(code).build())
                    .toList();
            default -> List.of();
        };
    }
}
