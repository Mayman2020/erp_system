package com.erp.system.alerts.repository;

import com.erp.system.alerts.domain.AlertEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {
    List<AlertEvent> findAllByOrderByCreatedAtDesc();

    List<AlertEvent> findByStatusOrderByCreatedAtDesc(String status);
}
