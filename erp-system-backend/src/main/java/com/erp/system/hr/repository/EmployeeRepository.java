package com.erp.system.hr.repository;

import com.erp.system.hr.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findAllByOrderByIdDesc();

    List<Employee> findByActiveTrueOrderByIdAsc();

    @Query("SELECT e FROM Employee e WHERE (:active IS NULL OR e.active = :active)")
    Page<Employee> findPaged(@Param("active") Boolean active, Pageable pageable);

    @Query("""
            SELECT e FROM Employee e
            WHERE (:active IS NULL OR e.active = :active)
              AND (LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(e.fullNameEn) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(e.fullNameAr, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(e.email, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(e.jobTitle, '')) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Employee> searchPaged(@Param("active") Boolean active,
                               @Param("q") String q,
                               Pageable pageable);
}
