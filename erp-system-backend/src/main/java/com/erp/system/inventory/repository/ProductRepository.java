package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByOrderByCodeAsc();

    List<Product> findByActiveTrueOrderByCodeAsc();

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByBarcodeIgnoreCase(String barcode);

    boolean existsByBarcodeIgnoreCaseAndIdNot(String barcode, Long id);

    Optional<Product> findByCodeIgnoreCase(String code);

    @Query("SELECT p FROM Product p WHERE LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.nameAr) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Product> search(@Param("search") String search);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId ORDER BY p.code")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    @Query("""
            SELECT p FROM Product p
            WHERE (:active IS NULL OR p.active = :active)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
            """)
    Page<Product> findPaged(@Param("active") Boolean active,
                            @Param("categoryId") Long categoryId,
                            Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE (:active IS NULL OR p.active = :active)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (LOWER(p.code) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nameEn) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(p.nameAr, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(p.barcode, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Product> searchPaged(@Param("active") Boolean active,
                              @Param("categoryId") Long categoryId,
                              @Param("q") String q,
                              Pageable pageable);

    boolean existsByUnit_Id(Long unitId);
}
