package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long> {
    List<PurchaseOrderLine> findAllByOrderByIdDesc();

    java.util.List<PurchaseOrderLine> findByOrderIdOrderByIdAsc(Long orderId);

}
