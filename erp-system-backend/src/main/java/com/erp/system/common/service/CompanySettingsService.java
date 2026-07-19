package com.erp.system.common.service;

import com.erp.system.common.dto.CompanySettingsDto;
import com.erp.system.common.dto.CompanySettingsUpdateDto;
import com.erp.system.common.entity.AccountingSettings;
import com.erp.system.common.repository.AccountingSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Company legal identity (name, tax id, logo, fiscal year start) — stored in the same generic
 * key-value settings table as accounting posting settings, but under a distinct key namespace
 * ("company.*") and its own service/controller, since it's a separate concern (legal/company
 * identity vs. accounting posting behavior).
 */
@Service
@RequiredArgsConstructor
public class CompanySettingsService {

    private static final String KEY_NAME_EN = "company.name_en";
    private static final String KEY_NAME_AR = "company.name_ar";
    private static final String KEY_TAX_ID = "company.tax_id";
    private static final String KEY_LOGO = "company.logo_base64";
    private static final String KEY_FISCAL_START_MONTH = "company.fiscal_year_start_month";

    private final AccountingSettingsRepository settingsRepository;

    @Transactional(readOnly = true)
    public CompanySettingsDto getSettings() {
        return CompanySettingsDto.builder()
                .companyNameEn(getValue(KEY_NAME_EN, ""))
                .companyNameAr(getValue(KEY_NAME_AR, ""))
                .taxId(getValue(KEY_TAX_ID, ""))
                .logoBase64(getValue(KEY_LOGO, null))
                .fiscalYearStartMonth(Integer.parseInt(getValue(KEY_FISCAL_START_MONTH, "1")))
                .build();
    }

    @Transactional
    public CompanySettingsDto updateSettings(CompanySettingsUpdateDto request) {
        upsert(KEY_NAME_EN, request.getCompanyNameEn(), "Company legal name (English)");
        upsert(KEY_NAME_AR, request.getCompanyNameAr(), "Company legal name (Arabic)");
        upsert(KEY_TAX_ID, request.getTaxId(), "Company tax identification number");
        upsert(KEY_LOGO, request.getLogoBase64(), "Company logo (base64)");
        upsert(KEY_FISCAL_START_MONTH, String.valueOf(request.getFiscalYearStartMonth() == null ? 1 : request.getFiscalYearStartMonth()), "Fiscal year start month (1-12)");
        return getSettings();
    }

    private String getValue(String key, String defaultValue) {
        return settingsRepository.findBySettingKey(key)
                .map(AccountingSettings::getSettingValue)
                .orElse(defaultValue);
    }

    private void upsert(String key, String value, String description) {
        AccountingSettings setting = settingsRepository.findBySettingKey(key)
                .orElseGet(AccountingSettings::new);
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setDescription(description);
        settingsRepository.save(setting);
    }
}
