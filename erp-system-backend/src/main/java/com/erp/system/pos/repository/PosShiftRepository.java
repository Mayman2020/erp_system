package com.erp.system.pos.repository;

import com.erp.system.pos.domain.PosShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosShiftRepository extends JpaRepository<PosShift, Long> {
    List<PosShift> findAllByOrderByOpenedAtDesc();
    Optional<PosShift> findFirstByCashier_IdAndStatusOrderByOpenedAtDesc(Long cashierId, String status);
    Optional<PosShift> findByShiftNo(String shiftNo);
}
