package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.StockMovement;
import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findAllByOrderByMovementDateDescIdDesc();

    List<StockMovement> findByStatusOrderByMovementDateDescIdDesc(TransactionStatus status);

    List<StockMovement> findByMovementTypeOrderByMovementDateDescIdDesc(StockMovementType movementType);

    List<StockMovement> findByProductIdOrderByMovementDateDescIdDesc(Long productId);

    List<StockMovement> findByWarehouseIdOrderByMovementDateDescIdDesc(Long warehouseId);

    boolean existsByMovementNumberIgnoreCase(String movementNumber);

    List<StockMovement> findByMovementDateBetweenOrderByMovementDateDescIdDesc(LocalDate fromDate, LocalDate toDate);
}
