package com.erp.system.common.repository;

import com.erp.system.common.entity.AccountingSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountingSettingsRepository extends JpaRepository<AccountingSettings, Long> {

    Optional<AccountingSettings> findBySettingKey(String settingKey);
}