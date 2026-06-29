package com.erp.system.accounting.repository;

import com.erp.system.accounting.domain.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findAllByOrderByTransferDateDescIdDesc();

    boolean existsByReferenceIgnoreCase(String reference);
}
