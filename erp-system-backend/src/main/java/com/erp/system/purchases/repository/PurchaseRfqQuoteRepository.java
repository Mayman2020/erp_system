package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.PurchaseRfqQuote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRfqQuoteRepository extends JpaRepository<PurchaseRfqQuote, Long> {

    List<PurchaseRfqQuote> findByRfqIdOrderByIdAsc(Long rfqId);
}
