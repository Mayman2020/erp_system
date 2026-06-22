package com.erp.system.sales.repository;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.sales.domain.SalesQuotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesQuotationRepository extends JpaRepository<SalesQuotation, Long> {

    List<SalesQuotation> findAllByOrderByQuotationDateDescIdDesc();

    List<SalesQuotation> findByStatusOrderByQuotationDateDescIdDesc(TransactionStatus status);

    boolean existsByQuotationNumberIgnoreCase(String quotationNumber);
}
