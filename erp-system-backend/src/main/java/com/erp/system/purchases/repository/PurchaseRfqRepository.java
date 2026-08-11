package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.PurchaseRfq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRfqRepository extends JpaRepository<PurchaseRfq, Long> {

    List<PurchaseRfq> findAllByOrderByIdDesc();

    boolean existsByRfqNoIgnoreCase(String rfqNo);
}
