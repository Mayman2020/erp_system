package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {

    List<UnitOfMeasure> findAllByOrderByCodeAsc();

    List<UnitOfMeasure> findByActiveTrueOrderByCodeAsc();

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @Query("SELECT u FROM UnitOfMeasure u WHERE LOWER(u.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nameAr) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<UnitOfMeasure> search(@Param("search") String search);
}
