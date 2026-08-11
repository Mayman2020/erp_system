package com.erp.system.hr.recruitment.repository;

import com.erp.system.hr.recruitment.domain.HrVacancy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrVacancyRepository extends JpaRepository<HrVacancy, Long> {
    List<HrVacancy> findAllByOrderByIdDesc();
}
