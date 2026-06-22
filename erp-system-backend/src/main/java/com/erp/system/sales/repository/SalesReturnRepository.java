package com.erp.system.sales.repository;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.sales.domain.SalesReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesReturnRepository extends JpaRepository<SalesReturn, Long> {

    List<SalesReturn> findAllByOrderByReturnDateDescIdDesc();

    List<SalesReturn> findByStatusOrderByReturnDateDescIdDesc(TransactionStatus status);

    boolean existsByReturnNumberIgnoreCase(String returnNumber);
}
