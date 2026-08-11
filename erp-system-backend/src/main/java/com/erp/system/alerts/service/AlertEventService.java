package com.erp.system.alerts.service;

import com.erp.system.alerts.domain.AlertEvent;
import com.erp.system.alerts.dto.display.AlertEventDisplayDto;
import com.erp.system.alerts.repository.AlertEventRepository;
import com.erp.system.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertEventService {

    private final AlertEventRepository alertEventRepository;

    @Transactional(readOnly = true)
    public List<AlertEventDisplayDto> getAll(String status) {
        List<AlertEvent> rows = status == null || status.isBlank()
                ? alertEventRepository.findAllByOrderByCreatedAtDesc()
                : alertEventRepository.findByStatusOrderByCreatedAtDesc(status);
        return rows.stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public AlertEventDisplayDto getById(Long id) {
        return toDisplay(load(id));
    }

    @Transactional
    public AlertEventDisplayDto acknowledge(Long id) {
        AlertEvent event = load(id);
        if (!"ACKNOWLEDGED".equals(event.getStatus())) {
            event.setStatus("ACKNOWLEDGED");
            event.setAcknowledgedAt(Instant.now());
            event = alertEventRepository.save(event);
        }
        return toDisplay(event);
    }

    private AlertEvent load(Long id) {
        return alertEventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("AlertEvent", id));
    }

    private AlertEventDisplayDto toDisplay(AlertEvent event) {
        return AlertEventDisplayDto.builder()
                .id(event.getId())
                .ruleId(event.getRuleId())
                .title(event.getTitle())
                .body(event.getBody())
                .severity(event.getSeverity())
                .entityType(event.getEntityType())
                .entityRef(event.getEntityRef())
                .deepLink(event.getDeepLink())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .acknowledgedAt(event.getAcknowledgedAt())
                .build();
    }
}
