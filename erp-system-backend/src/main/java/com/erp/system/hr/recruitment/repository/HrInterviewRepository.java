package com.erp.system.hr.recruitment.repository;

import com.erp.system.hr.recruitment.domain.HrInterview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HrInterviewRepository extends JpaRepository<HrInterview, Long> {
    List<HrInterview> findAllByOrderByScheduledAtDesc();

    List<HrInterview> findByCandidateIdOrderByScheduledAtDesc(Long candidateId);
}
