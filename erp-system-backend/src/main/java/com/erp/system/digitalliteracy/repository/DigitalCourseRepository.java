package com.erp.system.digitalliteracy.repository;

import com.erp.system.digitalliteracy.domain.DigitalCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DigitalCourseRepository extends JpaRepository<DigitalCourse, Long> {
    List<DigitalCourse> findAllByOrderByIdDesc();

    Optional<DigitalCourse> findByCodeIgnoreCase(String code);
}
