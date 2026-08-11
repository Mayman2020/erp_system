package com.erp.system.hr.recruitment.repository;

import com.erp.system.hr.recruitment.domain.HrCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrCandidateRepository extends JpaRepository<HrCandidate, Long> {
    List<HrCandidate> findAllByOrderByIdDesc();

    List<HrCandidate> findByVacancyIdOrderByIdDesc(Long vacancyId);
}
