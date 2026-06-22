package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findAllByOrderByCodeAsc();

    List<ProductCategory> findByActiveTrueOrderByCodeAsc();

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @Query("SELECT c FROM ProductCategory c WHERE LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.nameAr) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<ProductCategory> search(@Param("search") String search);
}
