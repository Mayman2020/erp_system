package com.erp.system.partners.repository;

import com.erp.system.partners.domain.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

    List<Partner> findAllByOrderByCodeAsc();

    List<Partner> findByActiveTrueOrderByCodeAsc();

    Optional<Partner> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
