package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.CustomerInvoice;
import com.erp.system.common.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerInvoiceRepository extends JpaRepository<CustomerInvoice, Long> {
    List<CustomerInvoice> findAllByOrderByInvoiceDateDescIdDesc();
    List<CustomerInvoice> findByStatusOrderByInvoiceDateDescIdDesc(InvoiceStatus status);
    boolean existsByInvoiceNumberIgnoreCase(String invoiceNumber);
    CustomerInvoice findByInvoiceNumber(String invoiceNumber);
}
