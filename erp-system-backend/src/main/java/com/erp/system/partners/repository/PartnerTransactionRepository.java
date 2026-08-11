package com.erp.system.partners.repository;

import com.erp.system.partners.domain.PartnerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnerTransactionRepository extends JpaRepository<PartnerTransaction, Long> {

    List<PartnerTransaction> findAllByOrderByTxnDateDescIdDesc();

    List<PartnerTransaction> findByPartnerIdOrderByTxnDateDescIdDesc(Long partnerId);
}
