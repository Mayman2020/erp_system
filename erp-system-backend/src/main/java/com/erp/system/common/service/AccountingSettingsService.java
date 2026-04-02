package com.erp.system.common.service;

import com.erp.system.common.entity.AccountingSettings;
import com.erp.system.common.repository.AccountingSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountingSettingsService {

    private final AccountingSettingsRepository settingsRepository;

    @Cacheable("accountingSettings")
    public String getSetting(String key) {
        return settingsRepository.findBySettingKey(key)
                .map(AccountingSettings::getSettingValue)
                .orElse(null);
    }

    @Cacheable("accountingSettings")
    public String getSetting(String key, String defaultValue) {
        String value = getSetting(key);
        return value != null ? value : defaultValue;
    }

    public boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = getSetting(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public int getIntSetting(String key, int defaultValue) {
        String value = getSetting(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
}