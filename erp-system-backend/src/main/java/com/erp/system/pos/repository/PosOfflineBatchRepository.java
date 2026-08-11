package com.erp.system.pos.repository;

import com.erp.system.pos.domain.PosOfflineBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PosOfflineBatchRepository extends JpaRepository<PosOfflineBatch, Long> {
    Optional<PosOfflineBatch> findByBatchKey(String batchKey);
}
