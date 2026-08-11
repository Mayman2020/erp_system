package com.erp.system.maintenance.repository;

import com.erp.system.maintenance.domain.MaintenanceTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, Long> {

    List<MaintenanceTicket> findAllByOrderByOpenedAtDescIdDesc();

    boolean existsByTicketNoIgnoreCase(String ticketNo);
}
