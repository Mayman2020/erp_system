package com.erp.system.hr.repository;

import com.erp.system.hr.domain.PayrollLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollLineRepository extends JpaRepository<PayrollLine, Long> {
    List<PayrollLine> findAllByOrderByIdDesc();

    java.util.List<PayrollLine> findByPayrollIdOrderByIdAsc(Long payrollId);

}
