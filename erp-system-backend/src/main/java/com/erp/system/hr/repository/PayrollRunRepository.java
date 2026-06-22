package com.erp.system.hr.repository;

import com.erp.system.hr.domain.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {
    List<PayrollRun> findAllByOrderByIdDesc();
    boolean existsByPayrollNumberIgnoreCase(String payrollNumber);
}
