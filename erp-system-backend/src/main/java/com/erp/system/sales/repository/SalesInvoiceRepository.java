package com.erp.system.sales.repository;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.sales.domain.SalesInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {

    List<SalesInvoice> findAllByOrderByInvoiceDateDescIdDesc();

    List<SalesInvoice> findByStatusOrderByInvoiceDateDescIdDesc(TransactionStatus status);

    boolean existsByInvoiceNumberIgnoreCase(String invoiceNumber);

    Optional<SalesInvoice> findByInvoiceNumber(String invoiceNumber);

    @Query("""
            SELECT i FROM SalesInvoice i
            WHERE (:status IS NULL OR i.status = :status)
              AND (:fromDate IS NULL OR i.invoiceDate >= :fromDate)
              AND (:toDate IS NULL OR i.invoiceDate <= :toDate)
            """)
    Page<SalesInvoice> findPaged(@Param("status") TransactionStatus status,
                                 @Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate,
                                 Pageable pageable);

    @Query("""
            SELECT i FROM SalesInvoice i
            WHERE (:status IS NULL OR i.status = :status)
              AND (:fromDate IS NULL OR i.invoiceDate >= :fromDate)
              AND (:toDate IS NULL OR i.invoiceDate <= :toDate)
              AND (LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(i.customer.code) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(i.customer.nameEn) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<SalesInvoice> searchPaged(@Param("status") TransactionStatus status,
                                   @Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate,
                                   @Param("q") String q,
                                   Pageable pageable);
}
