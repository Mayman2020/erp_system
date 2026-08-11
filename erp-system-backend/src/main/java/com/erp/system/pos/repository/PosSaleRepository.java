package com.erp.system.pos.repository;

import com.erp.system.pos.domain.PosSale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosSaleRepository extends JpaRepository<PosSale, Long> {
    Optional<PosSale> findByIdempotencyKey(String idempotencyKey);
    List<PosSale> findByShift_IdOrderByCreatedAtDesc(Long shiftId);
}
