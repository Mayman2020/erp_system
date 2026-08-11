package com.erp.system.hr.recruitment.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.recruitment.domain.HrVacancy;
import com.erp.system.hr.recruitment.dto.display.HrVacancyDisplayDto;
import com.erp.system.hr.recruitment.dto.form.HrVacancyFormDto;
import com.erp.system.hr.recruitment.repository.HrVacancyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrVacancyService {

    private static final String MODULE = "HR";

    private final HrVacancyRepository hrVacancyRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<HrVacancyDisplayDto> getAll() {
        return hrVacancyRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public HrVacancyDisplayDto getById(Long id) {
        return toDisplay(load(id));
    }

    @Transactional
    public HrVacancyDisplayDto create(HrVacancyFormDto request) {
        HrVacancy entity = new HrVacancy();
        applyForm(entity, request);
        entity = hrVacancyRepository.save(entity);
        activityLogService.log(MODULE, "CREATE", "HrVacancy", entity.getId(), entity.getTitle(), "Created vacancy " + entity.getTitle());
        return toDisplay(entity);
    }

    @Transactional
    public HrVacancyDisplayDto update(Long id, HrVacancyFormDto request) {
        HrVacancy entity = load(id);
        applyForm(entity, request);
        entity = hrVacancyRepository.save(entity);
        activityLogService.log(MODULE, "UPDATE", "HrVacancy", entity.getId(), entity.getTitle(), "Updated vacancy " + entity.getTitle());
        return toDisplay(entity);
    }

    @Transactional
    public void delete(Long id) {
        HrVacancy entity = load(id);
        hrVacancyRepository.delete(entity);
        activityLogService.log(MODULE, "DELETE", "HrVacancy", id, entity.getTitle(), "Deleted vacancy " + entity.getTitle());
    }

    private HrVacancy load(Long id) {
        return hrVacancyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("HrVacancy", id));
    }

    private void applyForm(HrVacancy entity, HrVacancyFormDto request) {
        entity.setTitle(request.getTitle());
        entity.setDepartmentId(request.getDepartmentId());
        entity.setStatus(request.getStatus());
        entity.setOpenings(request.getOpenings() == null ? 1 : request.getOpenings());
        entity.setDescription(request.getDescription());
    }

    private HrVacancyDisplayDto toDisplay(HrVacancy entity) {
        return HrVacancyDisplayDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .departmentId(entity.getDepartmentId())
                .status(entity.getStatus())
                .openings(entity.getOpenings())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
