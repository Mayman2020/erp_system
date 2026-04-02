package com.erp.system.accounting.service;

import com.erp.system.accounting.domain.AccountingAuditLog;
import com.erp.system.accounting.repository.AccountingAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountingAuditService {

    private final AccountingAuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, Long entityId, String action, String actor, String detail) {
        auditLogRepository.save(AccountingAuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .actor(actor)
                .detail(detail)
                .performedAt(Instant.now())
                .build());
    }

    @Transactional(readOnly = true)
    public List<AccountingAuditLog> getAuditTrail(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByPerformedAtDesc(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public List<AccountingAuditLog> getRecentActivity() {
        return auditLogRepository.findTop100ByOrderByPerformedAtDesc();
    }
}
