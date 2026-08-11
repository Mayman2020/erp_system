package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT c FROM ProductCategory c WHERE (:active IS NULL OR c.active = :active)")
    Page<ProductCategory> findPaged(@Param("active") Boolean active, Pageable pageable);

    @Query("""
            SELECT c FROM ProductCategory c
            WHERE (:active IS NULL OR c.active = :active)
              AND (LOWER(c.code) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(c.nameAr, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<ProductCategory> searchPaged(@Param("active") Boolean active,
                                      @Param("q") String q,
                                      Pageable pageable);
}
