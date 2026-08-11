package com.erp.system.digitalliteracy.repository;

import com.erp.system.digitalliteracy.domain.DigitalEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DigitalEnrollmentRepository extends JpaRepository<DigitalEnrollment, Long> {
    List<DigitalEnrollment> findAllByOrderByIdDesc();

    List<DigitalEnrollment> findByCourseIdOrderByIdDesc(Long courseId);

    List<DigitalEnrollment> findByEmployeeIdOrderByIdDesc(Long employeeId);

    Optional<DigitalEnrollment> findByCourseIdAndEmployeeId(Long courseId, Long employeeId);
}
