package com.erp.system.maintenance.repository;

import com.erp.system.maintenance.domain.MaintenanceAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceAssetRepository extends JpaRepository<MaintenanceAsset, Long> {

    List<MaintenanceAsset> findAllByOrderByAssetCodeAsc();

    boolean existsByAssetCodeIgnoreCase(String assetCode);

    Optional<MaintenanceAsset> findByAssetCodeIgnoreCase(String assetCode);
}
