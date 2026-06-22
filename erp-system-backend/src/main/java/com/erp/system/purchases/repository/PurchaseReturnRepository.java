package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.PurchaseReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturn, Long> {
    List<PurchaseReturn> findAllByOrderByIdDesc();

}
