package com.erp.system.hr.repository;

import com.erp.system.hr.domain.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {
    List<EmployeeDocument> findAllByOrderByIdDesc();

    List<EmployeeDocument> findByEmployeeIdOrderByIdDesc(Long employeeId);

}
