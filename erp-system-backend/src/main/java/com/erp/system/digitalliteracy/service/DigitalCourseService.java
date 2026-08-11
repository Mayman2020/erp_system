package com.erp.system.digitalliteracy.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.digitalliteracy.domain.DigitalCourse;
import com.erp.system.digitalliteracy.dto.display.DigitalCourseDisplayDto;
import com.erp.system.digitalliteracy.dto.form.DigitalCourseFormDto;
import com.erp.system.digitalliteracy.repository.DigitalCourseRepository;
import com.erp.system.erp.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DigitalCourseService {

    private static final String MODULE = "DIGITAL_LITERACY";

    private final DigitalCourseRepository digitalCourseRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<DigitalCourseDisplayDto> getAll() {
        return digitalCourseRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public DigitalCourseDisplayDto getById(Long id) {
        return toDisplay(load(id));
    }

    @Transactional
    public DigitalCourseDisplayDto create(DigitalCourseFormDto request) {
        digitalCourseRepository.findByCodeIgnoreCase(request.getCode()).ifPresent(existing -> {
            throw new BusinessException("Course code already exists");
        });
        DigitalCourse entity = new DigitalCourse();
        applyForm(entity, request);
        entity = digitalCourseRepository.save(entity);
        activityLogService.log(MODULE, "CREATE", "DigitalCourse", entity.getId(), entity.getCode(), "Created course");
        return toDisplay(entity);
    }

    @Transactional
    public DigitalCourseDisplayDto update(Long id, DigitalCourseFormDto request) {
        DigitalCourse entity = load(id);
        digitalCourseRepository.findByCodeIgnoreCase(request.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("Course code already exists");
            }
        });
        applyForm(entity, request);
        entity = digitalCourseRepository.save(entity);
        activityLogService.log(MODULE, "UPDATE", "DigitalCourse", entity.getId(), entity.getCode(), "Updated course");
        return toDisplay(entity);
    }

    @Transactional
    public void delete(Long id) {
        DigitalCourse entity = load(id);
        digitalCourseRepository.delete(entity);
        activityLogService.log(MODULE, "DELETE", "DigitalCourse", id, entity.getCode(), "Deleted course");
    }

    private DigitalCourse load(Long id) {
        return digitalCourseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("DigitalCourse", id));
    }

    private void applyForm(DigitalCourse entity, DigitalCourseFormDto request) {
        entity.setCode(request.getCode());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
    }

    private DigitalCourseDisplayDto toDisplay(DigitalCourse entity) {
        return DigitalCourseDisplayDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
