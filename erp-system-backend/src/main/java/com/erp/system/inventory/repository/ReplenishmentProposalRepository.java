package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.ReplenishmentProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReplenishmentProposalRepository extends JpaRepository<ReplenishmentProposal, Long> {

    List<ReplenishmentProposal> findAllByOrderByIdDesc();

    List<ReplenishmentProposal> findByStatusOrderByIdDesc(String status);

    Optional<ReplenishmentProposal> findByWarehouseIdAndProductIdAndStatus(Long warehouseId, Long productId, String status);
}
