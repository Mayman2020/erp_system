package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.StockMovement;
import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            SELECT m FROM StockMovement m
            WHERE (:movementType IS NULL OR m.movementType = :movementType)
              AND (:status IS NULL OR m.status = :status)
              AND (:productId IS NULL OR m.product.id = :productId)
              AND (:warehouseId IS NULL OR m.warehouse.id = :warehouseId)
              AND (:fromDate IS NULL OR m.movementDate >= :fromDate)
              AND (:toDate IS NULL OR m.movementDate <= :toDate)
            """)
    Page<StockMovement> findPaged(@Param("movementType") StockMovementType movementType,
                                  @Param("status") TransactionStatus status,
                                  @Param("productId") Long productId,
                                  @Param("warehouseId") Long warehouseId,
                                  @Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate,
                                  Pageable pageable);

    @Query("""
            SELECT m FROM StockMovement m
            WHERE (:movementType IS NULL OR m.movementType = :movementType)
              AND (:status IS NULL OR m.status = :status)
              AND (:productId IS NULL OR m.product.id = :productId)
              AND (:warehouseId IS NULL OR m.warehouse.id = :warehouseId)
              AND (:fromDate IS NULL OR m.movementDate >= :fromDate)
              AND (:toDate IS NULL OR m.movementDate <= :toDate)
              AND (LOWER(m.movementNumber) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(m.product.code) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(m.product.nameEn) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<StockMovement> searchPaged(@Param("movementType") StockMovementType movementType,
                                    @Param("status") TransactionStatus status,
                                    @Param("productId") Long productId,
                                    @Param("warehouseId") Long warehouseId,
                                    @Param("fromDate") LocalDate fromDate,
                                    @Param("toDate") LocalDate toDate,
                                    @Param("q") String q,
                                    Pageable pageable);
}
