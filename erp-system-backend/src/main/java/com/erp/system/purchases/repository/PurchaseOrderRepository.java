package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.PurchaseOrder;
import com.erp.system.common.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findAllByOrderByIdDesc();

    @Query("""
            SELECT o FROM PurchaseOrder o
            WHERE (:status IS NULL OR o.status = :status)
              AND (:fromDate IS NULL OR o.orderDate >= :fromDate)
              AND (:toDate IS NULL OR o.orderDate <= :toDate)
            """)
    Page<PurchaseOrder> findPaged(@Param("status") TransactionStatus status,
                                  @Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate,
                                  Pageable pageable);

    @Query("""
            SELECT o FROM PurchaseOrder o
            WHERE (:status IS NULL OR o.status = :status)
              AND (:fromDate IS NULL OR o.orderDate >= :fromDate)
              AND (:toDate IS NULL OR o.orderDate <= :toDate)
              AND LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<PurchaseOrder> searchPaged(@Param("status") TransactionStatus status,
                                    @Param("fromDate") LocalDate fromDate,
                                    @Param("toDate") LocalDate toDate,
                                    @Param("q") String q,
                                    Pageable pageable);
}
