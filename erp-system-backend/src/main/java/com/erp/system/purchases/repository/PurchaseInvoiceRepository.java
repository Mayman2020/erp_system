package com.erp.system.purchases.repository;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.purchases.domain.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {
    List<PurchaseInvoice> findAllByOrderByInvoiceDateDescIdDesc();
    List<PurchaseInvoice> findByStatusOrderByInvoiceDateDescIdDesc(TransactionStatus status);
    boolean existsByInvoiceNumberIgnoreCase(String invoiceNumber);
    boolean existsByInvoiceNumberIgnoreCaseAndIdNot(String invoiceNumber, Long id);
}
