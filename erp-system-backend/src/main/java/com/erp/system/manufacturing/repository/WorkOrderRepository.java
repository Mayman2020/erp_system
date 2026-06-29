package com.erp.system.manufacturing.repository;

import com.erp.system.manufacturing.domain.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findAllByOrderByPlannedStartDescIdDesc();

    Optional<WorkOrder> findByOrderNumberIgnoreCase(String orderNumber);

    boolean existsByOrderNumberIgnoreCase(String orderNumber);
}
