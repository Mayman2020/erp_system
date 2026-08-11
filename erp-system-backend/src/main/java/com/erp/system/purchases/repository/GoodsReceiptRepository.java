package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {

    List<GoodsReceipt> findAllByOrderByIdDesc();

    boolean existsByReceiptNoIgnoreCase(String receiptNo);
}
