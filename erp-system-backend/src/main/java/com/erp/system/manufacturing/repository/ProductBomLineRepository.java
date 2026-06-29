package com.erp.system.manufacturing.repository;

import com.erp.system.manufacturing.domain.ProductBomLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductBomLineRepository extends JpaRepository<ProductBomLine, Long> {

    List<ProductBomLine> findByParentProductIdOrderByIdAsc(Long parentProductId);
}
