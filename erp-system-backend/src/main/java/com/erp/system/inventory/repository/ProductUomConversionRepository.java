package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.ProductUomConversion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductUomConversionRepository extends JpaRepository<ProductUomConversion, Long> {

    List<ProductUomConversion> findByProductIdOrderByIdAsc(Long productId);

    Optional<ProductUomConversion> findByIdAndProductId(Long id, Long productId);

    boolean existsByProductIdAndUnitId(Long productId, Long unitId);
}
