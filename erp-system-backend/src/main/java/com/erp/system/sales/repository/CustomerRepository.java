package com.erp.system.sales.repository;

import com.erp.system.sales.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByOrderByCodeAsc();

    List<Customer> findByActiveTrueOrderByCodeAsc();

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    Optional<Customer> findByCodeIgnoreCase(String code);

    @Query("SELECT c FROM Customer c WHERE (:active IS NULL OR c.active = :active)")
    Page<Customer> findPaged(@Param("active") Boolean active, Pageable pageable);

    @Query("""
            SELECT c FROM Customer c
            WHERE (:active IS NULL OR c.active = :active)
              AND (LOWER(c.code) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(c.nameEn) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(c.nameAr, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(c.phone, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Customer> searchPaged(@Param("active") Boolean active,
                               @Param("q") String q,
                               Pageable pageable);
}
