package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.PurchaseRfqLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRfqLineRepository extends JpaRepository<PurchaseRfqLine, Long> {

    List<PurchaseRfqLine> findByRfqIdOrderByIdAsc(Long rfqId);
}
