package com.erp.system.notification.service;

import com.erp.system.auth.domain.UserRole;
import com.erp.system.auth.repository.UserRepository;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.security.JwtPrincipal;
import com.erp.system.notification.domain.Notification;
import com.erp.system.notification.domain.NotificationType;
import com.erp.system.notification.dto.NotificationPageResponse;
import com.erp.system.notification.dto.NotificationResponse;
import com.erp.system.notification.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /** Convenience for business-event producers: notify every active ADMIN user. */
    @Transactional
    public void notifyAdmins(NotificationType type, String titleKey, String bodyKey, Map<String, Object> vars,
                              String referenceType, Long referenceId) {
        var adminIds = userRepository.findByRoleAndActiveTrue(UserRole.ADMIN).stream().map(u -> u.getId()).toList();
        if (adminIds.isEmpty()) {
            return;
        }
        createForRecipients(adminIds, null, type, titleKey, bodyKey, vars, referenceType, referenceId);
    }

    @Transactional(readOnly = true)
    public NotificationPageResponse getMy(int page, int size) {
        Page<Notification> result = repository.findByRecipientUserIdOrderByCreatedAtDesc(
                currentUserId(), PageRequest.of(Math.max(page, 0), Math.max(size, 1)));
        return NotificationPageResponse.builder()
                .content(result.getContent().stream().map(this::toResponse).toList())
                .totalElements(result.getTotalElements())
                .page(result.getNumber())
                .size(result.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return repository.countByRecipientUserIdAndReadAtIsNull(currentUserId());
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        Notification notification = repository.findByIdAndRecipientUserId(id, currentUserId())
                .orElseThrow(() -> new BusinessException("NOTIFICATIONS.NOT_FOUND"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notification = repository.save(notification);
        }
        return toResponse(notification);
    }

    @Transactional
    public void markAllRead() {
        repository.markAllRead(currentUserId(), Instant.now());
    }

    @Transactional
    public void createForRecipients(Collection<Long> recipientIds, Long actorUserId, NotificationType type,
                                    String titleKey, String bodyKey, Map<String, Object> vars,
                                    String referenceType, Long referenceId) {
        String varsJson = toJson(vars);
        recipientIds.stream().distinct().forEach(recipientId -> repository.save(Notification.builder()
                .recipientUserId(recipientId)
                .actorUserId(actorUserId)
                .type(type)
                .titleKey(titleKey)
                .bodyKey(bodyKey)
                .varsJson(varsJson)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build()));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .titleKey(notification.getTitleKey())
                .bodyKey(notification.getBodyKey())
                .varsJson(notification.getVarsJson())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .read(notification.getReadAt() != null)
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private String toJson(Map<String, Object> vars) {
        if (vars == null || vars.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(vars);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof JwtPrincipal jwtPrincipal) {
            return jwtPrincipal.userId();
        }
        throw new BusinessException("AUTH.ERRORS.INVALID_REQUEST");
    }
}
