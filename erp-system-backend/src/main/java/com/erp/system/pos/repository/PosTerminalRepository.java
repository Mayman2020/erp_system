package com.erp.system.pos.repository;

import com.erp.system.pos.domain.PosTerminal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PosTerminalRepository extends JpaRepository<PosTerminal, Long> {
    List<PosTerminal> findByActiveTrueOrderByCodeAsc();
    Optional<PosTerminal> findByCodeIgnoreCase(String code);
}
