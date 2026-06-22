package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {

    Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<StockLevel> findByProductIdOrderByWarehouse_CodeAsc(Long productId);

    List<StockLevel> findByWarehouseIdOrderByProduct_CodeAsc(Long warehouseId);

    List<StockLevel> findAllByOrderByProduct_CodeAscWarehouse_CodeAsc();

    @Query("SELECT COALESCE(SUM(sl.quantity), 0) FROM StockLevel sl WHERE sl.product.id = :productId")
    BigDecimal sumQuantityByProductId(@Param("productId") Long productId);

    @Query("""
            SELECT sl FROM StockLevel sl
            JOIN sl.product p
            WHERE p.active = true
              AND p.reorderLevel > 0
              AND (SELECT COALESCE(SUM(s.quantity), 0) FROM StockLevel s WHERE s.product.id = p.id) <= p.reorderLevel
            ORDER BY p.code
            """)
    List<StockLevel> findLowStockLevels();
}
