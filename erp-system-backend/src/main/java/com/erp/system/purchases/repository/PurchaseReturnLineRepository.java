package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.PurchaseReturnLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseReturnLineRepository extends JpaRepository<PurchaseReturnLine, Long> {
    List<PurchaseReturnLine> findAllByOrderByIdDesc();

    java.util.List<PurchaseReturnLine> findByReturnIdOrderByIdAsc(Long returnId);

}
