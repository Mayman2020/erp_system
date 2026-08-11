package com.erp.system.hr.recruitment.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.recruitment.domain.HrInterview;
import com.erp.system.hr.recruitment.dto.display.HrInterviewDisplayDto;
import com.erp.system.hr.recruitment.dto.form.HrInterviewFormDto;
import com.erp.system.hr.recruitment.repository.HrCandidateRepository;
import com.erp.system.hr.recruitment.repository.HrInterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrInterviewService {

    private static final String MODULE = "HR";

    private final HrInterviewRepository hrInterviewRepository;
    private final HrCandidateRepository hrCandidateRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<HrInterviewDisplayDto> getAll(Long candidateId) {
        List<HrInterview> rows = candidateId == null
                ? hrInterviewRepository.findAllByOrderByScheduledAtDesc()
                : hrInterviewRepository.findByCandidateIdOrderByScheduledAtDesc(candidateId);
        return rows.stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public HrInterviewDisplayDto getById(Long id) {
        return toDisplay(load(id));
    }

    @Transactional
    public HrInterviewDisplayDto create(HrInterviewFormDto request) {
        hrCandidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("HrCandidate", request.getCandidateId()));
        HrInterview entity = new HrInterview();
        applyForm(entity, request);
        entity = hrInterviewRepository.save(entity);
        activityLogService.log(MODULE, "CREATE", "HrInterview", entity.getId(), String.valueOf(entity.getId()), "Scheduled interview");
        return toDisplay(entity);
    }

    @Transactional
    public HrInterviewDisplayDto update(Long id, HrInterviewFormDto request) {
        HrInterview entity = load(id);
        applyForm(entity, request);
        entity = hrInterviewRepository.save(entity);
        activityLogService.log(MODULE, "UPDATE", "HrInterview", entity.getId(), String.valueOf(entity.getId()), "Updated interview");
        return toDisplay(entity);
    }

    @Transactional
    public void delete(Long id) {
        HrInterview entity = load(id);
        hrInterviewRepository.delete(entity);
        activityLogService.log(MODULE, "DELETE", "HrInterview", id, String.valueOf(id), "Deleted interview");
    }

    private HrInterview load(Long id) {
        return hrInterviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("HrInterview", id));
    }

    private void applyForm(HrInterview entity, HrInterviewFormDto request) {
        entity.setCandidateId(request.getCandidateId());
        entity.setScheduledAt(request.getScheduledAt());
        entity.setInterviewer(request.getInterviewer());
        entity.setResult(request.getResult());
        entity.setNotes(request.getNotes());
    }

    private HrInterviewDisplayDto toDisplay(HrInterview entity) {
        return HrInterviewDisplayDto.builder()
                .id(entity.getId())
                .candidateId(entity.getCandidateId())
                .scheduledAt(entity.getScheduledAt())
                .interviewer(entity.getInterviewer())
                .result(entity.getResult())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
