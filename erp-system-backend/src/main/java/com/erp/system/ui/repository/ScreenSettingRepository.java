package com.erp.system.ui.repository;

import com.erp.system.ui.domain.ScreenSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenSettingRepository extends JpaRepository<ScreenSetting, String> {
    List<ScreenSetting> findAllByOrderByScreenKeyAsc();
}
