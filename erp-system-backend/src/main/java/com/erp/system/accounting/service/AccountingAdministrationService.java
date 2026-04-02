package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.FiscalPeriod;
import com.erp.system.accounting.domain.FiscalYear;
import com.erp.system.accounting.dto.display.AccountingSettingsDisplayDto;
import com.erp.system.accounting.dto.display.FiscalPeriodDisplayDto;
import com.erp.system.accounting.dto.display.FiscalYearDisplayDto;
import com.erp.system.accounting.dto.display.NumberingSequenceDisplayDto;
import com.erp.system.accounting.dto.form.AccountingSettingsUpdateDto;
import com.erp.system.accounting.dto.form.FiscalPeriodFormDto;
import com.erp.system.accounting.dto.form.FiscalYearFormDto;
import com.erp.system.accounting.repository.FiscalPeriodRepository;
import com.erp.system.accounting.repository.FiscalYearRepository;
import com.erp.system.common.entity.AccountingSettings;
import com.erp.system.common.entity.NumberingSequence;
import com.erp.system.common.enums.AccountingMethod;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.repository.AccountingSettingsRepository;
import com.erp.system.common.repository.NumberingSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountingAdministrationService {

    private final AccountingSettingsRepository settingsRepository;
    private final NumberingSequenceRepository numberingSequenceRepository;
    private final FiscalYearRepository fiscalYearRepository;
    private final FiscalPeriodRepository fiscalPeriodRepository;

    @Transactional(readOnly = true)
    public AccountingSettingsDisplayDto getSettings() {
        return AccountingSettingsDisplayDto.builder()
                .accountingMethod(AccountingMethod.valueOf(getSettingValue("ACCOUNTING_METHOD", "ACCRUAL")))
                .baseCurrency(getSettingValue("BASE_CURRENCY", "USD"))
                .allowedCurrencies(getSettingValue("ALLOWED_CURRENCIES", "USD"))
                .sequences(numberingSequenceRepository.findAll().stream().map(this::toDisplay).toList())
                .fiscalYears(fiscalYearRepository.findAllByOrderByYearDesc().stream().map(this::toDisplay).toList())
                .build();
    }

    @Transactional
    public AccountingSettingsDisplayDto updateSettings(AccountingSettingsUpdateDto request) {
        Set<String> allowedCurrencies = normalizeCurrencies(request.getAllowedCurrencies());
        String baseCurrency = request.getBaseCurrency().trim().toUpperCase(Locale.ROOT);
        if (allowedCurrencies.isEmpty()) {
            throw new BusinessException("At least one allowed currency is required");
        }
        if (!allowedCurrencies.contains(baseCurrency)) {
            throw new BusinessException("Base currency must be included in allowed currencies");
        }

        upsertSetting("ACCOUNTING_METHOD", request.getAccountingMethod().name(), "Accounting method");
        upsertSetting("BASE_CURRENCY", baseCurrency, "Base currency");
        upsertSetting("ALLOWED_CURRENCIES", String.join(",", allowedCurrencies), "Allowed currencies");
        return getSettings();
    }

    @Transactional
    public FiscalYearDisplayDto createFiscalYear(FiscalYearFormDto request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("Fiscal year end date cannot be before start date");
        }
        if (fiscalYearRepository.findByYear(request.getYear()).isPresent()) {
            throw new BusinessException("Fiscal year already exists");
        }
        boolean overlapsExistingYear = fiscalYearRepository.findAll().stream()
                .anyMatch(year -> rangesOverlap(request.getStartDate(), request.getEndDate(), year.getStartDate(), year.getEndDate()));
        if (overlapsExistingYear) {
            throw new BusinessException("Fiscal year dates overlap with an existing fiscal year");
        }
        FiscalYear fiscalYear = FiscalYear.builder()
                .year(request.getYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .open(true)
                .build();
        return toDisplay(fiscalYearRepository.save(fiscalYear));
    }

    @Transactional
    public FiscalPeriodDisplayDto createFiscalPeriod(Long fiscalYearId, FiscalPeriodFormDto request) {
        FiscalYear fiscalYear = fiscalYearRepository.findById(fiscalYearId)
                .orElseThrow(() -> new ResourceNotFoundException("FiscalYear", fiscalYearId));
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("Fiscal period end date cannot be before start date");
        }
        if (request.getStartDate().isBefore(fiscalYear.getStartDate()) || request.getEndDate().isAfter(fiscalYear.getEndDate())) {
            throw new BusinessException("Fiscal period dates must stay within the selected fiscal year");
        }
        boolean overlapsExistingPeriod = fiscalPeriodRepository.findByFiscalYearIdOrderByStartDateAsc(fiscalYearId).stream()
                .anyMatch(period -> rangesOverlap(request.getStartDate(), request.getEndDate(), period.getStartDate(), period.getEndDate()));
        if (overlapsExistingPeriod) {
            throw new BusinessException("Fiscal period dates overlap with an existing fiscal period");
        }
        FiscalPeriod period = FiscalPeriod.builder()
                .fiscalYear(fiscalYear)
                .periodName(request.getPeriodName().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .open(true)
                .build();
        return toDisplay(fiscalPeriodRepository.save(period));
    }

    @Transactional
    public FiscalYearDisplayDto closeFiscalYear(Long id, String actor) {
        FiscalYear year = loadFiscalYear(id);
        year.setOpen(false);
        year.setClosedAt(LocalDateTime.now());
        year.setClosedBy(actor);
        return toDisplay(fiscalYearRepository.save(year));
    }

    @Transactional
    public FiscalYearDisplayDto openFiscalYear(Long id) {
        FiscalYear year = loadFiscalYear(id);
        year.setOpen(true);
        year.setClosedAt(null);
        year.setClosedBy(null);
        return toDisplay(fiscalYearRepository.save(year));
    }

    @Transactional
    public FiscalPeriodDisplayDto closeFiscalPeriod(Long id, String actor) {
        FiscalPeriod period = loadFiscalPeriod(id);
        period.setOpen(false);
        period.setClosedAt(LocalDateTime.now());
        period.setClosedBy(actor);
        return toDisplay(fiscalPeriodRepository.save(period));
    }

    @Transactional
    public FiscalPeriodDisplayDto openFiscalPeriod(Long id) {
        FiscalPeriod period = loadFiscalPeriod(id);
        period.setOpen(true);
        period.setClosedAt(null);
        period.setClosedBy(null);
        return toDisplay(fiscalPeriodRepository.save(period));
    }

    private String getSettingValue(String key, String defaultValue) {
        return settingsRepository.findBySettingKey(key)
                .map(AccountingSettings::getSettingValue)
                .orElse(defaultValue);
    }

    private void upsertSetting(String key, String value, String description) {
        AccountingSettings setting = settingsRepository.findBySettingKey(key)
                .orElseGet(AccountingSettings::new);
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setDescription(description);
        settingsRepository.save(setting);
    }

    private FiscalYear loadFiscalYear(Long id) {
        return fiscalYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FiscalYear", id));
    }

    private FiscalPeriod loadFiscalPeriod(Long id) {
        return fiscalPeriodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FiscalPeriod", id));
    }

    private NumberingSequenceDisplayDto toDisplay(NumberingSequence sequence) {
        return NumberingSequenceDisplayDto.builder()
                .id(sequence.getId())
                .sequenceName(sequence.getSequenceName())
                .prefix(sequence.getPrefix())
                .currentNumber(sequence.getCurrentNumber())
                .paddingLength(sequence.getPaddingLength())
                .build();
    }

    private FiscalYearDisplayDto toDisplay(FiscalYear year) {
        List<FiscalPeriodDisplayDto> periods = fiscalPeriodRepository.findByFiscalYearIdOrderByStartDateAsc(year.getId())
                .stream()
                .map(this::toDisplay)
                .toList();
        return FiscalYearDisplayDto.builder()
                .id(year.getId())
                .year(year.getYear())
                .startDate(year.getStartDate())
                .endDate(year.getEndDate())
                .open(year.isOpen())
                .closedAt(year.getClosedAt())
                .closedBy(year.getClosedBy())
                .periods(periods)
                .createdAt(year.getCreatedAt())
                .updatedAt(year.getUpdatedAt())
                .build();
    }

    private FiscalPeriodDisplayDto toDisplay(FiscalPeriod period) {
        return FiscalPeriodDisplayDto.builder()
                .id(period.getId())
                .fiscalYearId(period.getFiscalYear().getId())
                .periodName(period.getPeriodName())
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .open(period.isOpen())
                .closedAt(period.getClosedAt())
                .closedBy(period.getClosedBy())
                .createdAt(period.getCreatedAt())
                .updatedAt(period.getUpdatedAt())
                .build();
    }

    private Set<String> normalizeCurrencies(String currencies) {
        return Arrays.stream((currencies == null ? "" : currencies).split(","))
                .map(value -> value == null ? "" : value.trim().toUpperCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private boolean rangesOverlap(LocalDate firstStart, LocalDate firstEnd, LocalDate secondStart, LocalDate secondEnd) {
        return !firstEnd.isBefore(secondStart) && !secondEnd.isBefore(firstStart);
    }
}
