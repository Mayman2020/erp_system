package com.erp.system.hr.recruitment.repository;

import com.erp.system.hr.recruitment.domain.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findAllByOrderByYearDescEmployeeIdAsc();

    List<LeaveBalance> findByEmployeeIdOrderByYearDesc(Long employeeId);

    List<LeaveBalance> findByYearOrderByEmployeeIdAsc(Integer year);
}
