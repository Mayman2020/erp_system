package com.erp.system.ui.service;

import com.erp.system.common.security.SecurityUtils;
import com.erp.system.ui.domain.ScreenSetting;
import com.erp.system.ui.dto.ScreenSettingDto;
import com.erp.system.ui.dto.ScreenSettingFormDto;
import com.erp.system.ui.repository.ScreenSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenSettingsService {

    private final ScreenSettingRepository screenSettingRepository;

    @Transactional(readOnly = true)
    public List<ScreenSettingDto> list() {
        return screenSettingRepository.findAllByOrderByScreenKeyAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ScreenSettingDto upsert(String screenKey, ScreenSettingFormDto request) {
        ScreenSetting setting = screenSettingRepository.findById(screenKey).orElseGet(() -> {
            ScreenSetting created = new ScreenSetting();
            created.setScreenKey(screenKey);
            return created;
        });
        setting.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        setting.setUpdatedBy(SecurityUtils.currentUsername());
        setting.setUpdatedAt(Instant.now());
        return toDto(screenSettingRepository.save(setting));
    }

    private ScreenSettingDto toDto(ScreenSetting setting) {
        return ScreenSettingDto.builder()
                .screenKey(setting.getScreenKey())
                .enabled(setting.isEnabled())
                .updatedBy(setting.getUpdatedBy())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
