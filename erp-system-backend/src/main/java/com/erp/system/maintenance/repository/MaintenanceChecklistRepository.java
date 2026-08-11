package com.erp.system.maintenance.repository;

import com.erp.system.maintenance.domain.MaintenanceChecklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceChecklistRepository extends JpaRepository<MaintenanceChecklist, Long> {

    List<MaintenanceChecklist> findByTicketIdOrderBySortOrderAscIdAsc(Long ticketId);

    void deleteByTicketId(Long ticketId);
}
