package com.erp.system.partners.repository;

import com.erp.system.partners.domain.ProfitDistribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfitDistributionRepository extends JpaRepository<ProfitDistribution, Long> {

    List<ProfitDistribution> findAllByOrderByIdDesc();

    Optional<ProfitDistribution> findByDistributionNoIgnoreCase(String distributionNo);

    boolean existsByDistributionNoIgnoreCase(String distributionNo);
}
