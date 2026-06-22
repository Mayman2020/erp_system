package com.erp.system.purchases.repository;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.purchases.domain.SupplierPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {
    List<SupplierPayment> findAllByOrderByIdDesc();
    List<SupplierPayment> findByStatusOrderByIdDesc(TransactionStatus status);
    boolean existsByPaymentNumberIgnoreCase(String paymentNumber);
}
