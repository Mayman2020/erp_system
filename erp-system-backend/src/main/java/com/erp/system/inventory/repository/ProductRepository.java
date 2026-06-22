package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.Product;
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

    boolean existsByUnit_Id(Long unitId);
}
