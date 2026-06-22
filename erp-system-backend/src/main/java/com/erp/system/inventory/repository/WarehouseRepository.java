package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findAllByOrderByCodeAsc();

    List<Warehouse> findByActiveTrueOrderByCodeAsc();

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @Query("SELECT w FROM Warehouse w WHERE LOWER(w.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.nameEn) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.nameAr) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.location) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Warehouse> search(@Param("search") String search);
}
