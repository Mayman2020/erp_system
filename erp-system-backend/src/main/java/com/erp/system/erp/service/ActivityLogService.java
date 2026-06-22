package com.erp.system.erp.service;

import com.erp.system.common.dto.PageResultDto;
import com.erp.system.common.security.SecurityUtils;
import com.erp.system.erp.domain.ActivityLog;
import com.erp.system.erp.dto.display.ActivityLogDisplayDto;
import com.erp.system.erp.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(String module, String action, String entityType, Long entityId, String reference, String description) {
        activityLogRepository.save(ActivityLog.builder()
                .moduleName(module)
                .actionType(action)
                .entityType(entityType)
                .entityId(entityId)
                .entityReference(reference)
                .description(description)
                .actor(SecurityUtils.currentUsername())
                .build());
    }

    @Transactional(readOnly = true)
    public PageResultDto<ActivityLogDisplayDto> getRecent(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLog> result = activityLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        int totalPages = result.getTotalPages();
        if (totalPages == 0 && result.getTotalElements() == 0) {
            totalPages = 1;
        }
        return PageResultDto.<ActivityLogDisplayDto>builder()
                .items(result.map(this::toDisplay).getContent())
                .page(result.getNumber())
                .size(result.getSize())
                .totalItems(result.getTotalElements())
                .totalPages(totalPages)
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .build();
    }

    private ActivityLogDisplayDto toDisplay(ActivityLog log) {
        return ActivityLogDisplayDto.builder()
                .id(log.getId())
                .moduleName(log.getModuleName())
                .actionType(log.getActionType())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .entityReference(log.getEntityReference())
                .description(log.getDescription())
                .actor(log.getActor())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
