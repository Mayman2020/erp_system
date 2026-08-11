package com.erp.system.maintenance.repository;

import com.erp.system.maintenance.domain.MaintenanceTechnician;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceTechnicianRepository extends JpaRepository<MaintenanceTechnician, Long> {

    List<MaintenanceTechnician> findAllByOrderByDisplayNameAsc();

    List<MaintenanceTechnician> findByActiveTrueOrderByDisplayNameAsc();
}
