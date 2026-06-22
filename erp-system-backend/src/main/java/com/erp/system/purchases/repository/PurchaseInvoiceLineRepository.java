package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.PurchaseInvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseInvoiceLineRepository extends JpaRepository<PurchaseInvoiceLine, Long> {
    List<PurchaseInvoiceLine> findAllByOrderByIdDesc();

    java.util.List<PurchaseInvoiceLine> findByInvoiceIdOrderByIdAsc(Long invoiceId);

}
