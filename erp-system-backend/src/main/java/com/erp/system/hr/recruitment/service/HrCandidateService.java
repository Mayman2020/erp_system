package com.erp.system.hr.recruitment.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.recruitment.domain.HrCandidate;
import com.erp.system.hr.recruitment.dto.display.HrCandidateDisplayDto;
import com.erp.system.hr.recruitment.dto.form.HrCandidateFormDto;
import com.erp.system.hr.recruitment.repository.HrCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrCandidateService {

    private static final String MODULE = "HR";

    private final HrCandidateRepository hrCandidateRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<HrCandidateDisplayDto> getAll(Long vacancyId) {
        List<HrCandidate> rows = vacancyId == null
                ? hrCandidateRepository.findAllByOrderByIdDesc()
                : hrCandidateRepository.findByVacancyIdOrderByIdDesc(vacancyId);
        return rows.stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public HrCandidateDisplayDto getById(Long id) {
        return toDisplay(load(id));
    }

    @Transactional
    public HrCandidateDisplayDto create(HrCandidateFormDto request) {
        HrCandidate entity = new HrCandidate();
        applyForm(entity, request);
        entity = hrCandidateRepository.save(entity);
        activityLogService.log(MODULE, "CREATE", "HrCandidate", entity.getId(), entity.getFullName(), "Created candidate " + entity.getFullName());
        return toDisplay(entity);
    }

    @Transactional
    public HrCandidateDisplayDto update(Long id, HrCandidateFormDto request) {
        HrCandidate entity = load(id);
        applyForm(entity, request);
        entity = hrCandidateRepository.save(entity);
        activityLogService.log(MODULE, "UPDATE", "HrCandidate", entity.getId(), entity.getFullName(), "Updated candidate " + entity.getFullName());
        return toDisplay(entity);
    }

    @Transactional
    public void delete(Long id) {
        HrCandidate entity = load(id);
        hrCandidateRepository.delete(entity);
        activityLogService.log(MODULE, "DELETE", "HrCandidate", id, entity.getFullName(), "Deleted candidate " + entity.getFullName());
    }

    private HrCandidate load(Long id) {
        return hrCandidateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("HrCandidate", id));
    }

    private void applyForm(HrCandidate entity, HrCandidateFormDto request) {
        entity.setFullName(request.getFullName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setVacancyId(request.getVacancyId());
        entity.setStatus(request.getStatus());
        entity.setScore(request.getScore());
        entity.setNotes(request.getNotes());
    }

    private HrCandidateDisplayDto toDisplay(HrCandidate entity) {
        return HrCandidateDisplayDto.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .vacancyId(entity.getVacancyId())
                .status(entity.getStatus())
                .score(entity.getScore())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
