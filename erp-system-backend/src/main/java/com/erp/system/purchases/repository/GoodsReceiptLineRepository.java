package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.GoodsReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsReceiptLineRepository extends JpaRepository<GoodsReceiptLine, Long> {

    List<GoodsReceiptLine> findByReceiptIdOrderByIdAsc(Long receiptId);
}
