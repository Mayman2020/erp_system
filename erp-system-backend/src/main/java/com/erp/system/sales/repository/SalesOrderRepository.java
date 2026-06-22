package com.erp.system.sales.repository;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.sales.domain.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    List<SalesOrder> findAllByOrderByOrderDateDescIdDesc();

    List<SalesOrder> findByStatusOrderByOrderDateDescIdDesc(TransactionStatus status);

    boolean existsByOrderNumberIgnoreCase(String orderNumber);
}
