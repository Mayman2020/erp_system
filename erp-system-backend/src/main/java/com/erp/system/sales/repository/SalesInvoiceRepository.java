package com.erp.system.sales.repository;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.sales.domain.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {

    List<SalesInvoice> findAllByOrderByInvoiceDateDescIdDesc();

    List<SalesInvoice> findByStatusOrderByInvoiceDateDescIdDesc(TransactionStatus status);

    boolean existsByInvoiceNumberIgnoreCase(String invoiceNumber);

    Optional<SalesInvoice> findByInvoiceNumber(String invoiceNumber);
}
