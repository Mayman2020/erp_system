package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.StockIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockIncidentRepository extends JpaRepository<StockIncident, Long> {

    List<StockIncident> findAllByOrderByIdDesc();

    boolean existsByIncidentNoIgnoreCase(String incidentNo);
}
