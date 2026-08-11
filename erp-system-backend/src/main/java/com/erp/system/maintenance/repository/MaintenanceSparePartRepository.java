package com.erp.system.maintenance.repository;

import com.erp.system.maintenance.domain.MaintenanceSparePart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceSparePartRepository extends JpaRepository<MaintenanceSparePart, Long> {

    List<MaintenanceSparePart> findByTicketIdOrderByIdAsc(Long ticketId);

    void deleteByTicketId(Long ticketId);
}
