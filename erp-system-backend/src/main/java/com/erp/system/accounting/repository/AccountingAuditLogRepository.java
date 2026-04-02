package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.AccountingAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountingAuditLogRepository extends JpaRepository<AccountingAuditLog, Long> {

    List<AccountingAuditLog> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(String entityType, Long entityId);

    List<AccountingAuditLog> findByActorOrderByPerformedAtDesc(String actor);

    List<AccountingAuditLog> findTop100ByOrderByPerformedAtDesc();
}
