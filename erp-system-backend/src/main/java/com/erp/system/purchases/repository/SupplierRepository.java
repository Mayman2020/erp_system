package com.erp.system.purchases.repository;

import com.erp.system.purchases.domain.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findAllByOrderByIdDesc();

    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @Query("SELECT s FROM Supplier s WHERE (:active IS NULL OR s.active = :active)")
    Page<Supplier> findPaged(@Param("active") Boolean active, Pageable pageable);

    @Query("""
            SELECT s FROM Supplier s
            WHERE (:active IS NULL OR s.active = :active)
              AND (LOWER(s.code) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(s.nameEn) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(s.nameAr, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(s.email, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(s.phone, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Supplier> searchPaged(@Param("active") Boolean active,
                               @Param("q") String q,
                               Pageable pageable);
}
