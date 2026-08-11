package com.erp.system.digitalliteracy.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.digitalliteracy.domain.DigitalEnrollment;
import com.erp.system.digitalliteracy.dto.display.DigitalEnrollmentDisplayDto;
import com.erp.system.digitalliteracy.dto.form.DigitalEnrollmentFormDto;
import com.erp.system.digitalliteracy.repository.DigitalCourseRepository;
import com.erp.system.digitalliteracy.repository.DigitalEnrollmentRepository;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DigitalEnrollmentService {

    private static final String MODULE = "DIGITAL_LITERACY";

    private final DigitalEnrollmentRepository digitalEnrollmentRepository;
    private final DigitalCourseRepository digitalCourseRepository;
    private final EmployeeRepository employeeRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<DigitalEnrollmentDisplayDto> getAll(Long courseId, Long employeeId) {
        List<DigitalEnrollment> rows;
        if (courseId != null) {
            rows = digitalEnrollmentRepository.findByCourseIdOrderByIdDesc(courseId);
        } else if (employeeId != null) {
            rows = digitalEnrollmentRepository.findByEmployeeIdOrderByIdDesc(employeeId);
        } else {
            rows = digitalEnrollmentRepository.findAllByOrderByIdDesc();
        }
        return rows.stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public DigitalEnrollmentDisplayDto getById(Long id) {
        return toDisplay(load(id));
    }

    @Transactional
    public DigitalEnrollmentDisplayDto create(DigitalEnrollmentFormDto request) {
        digitalCourseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("DigitalCourse", request.getCourseId()));
        employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", request.getEmployeeId()));
        digitalEnrollmentRepository.findByCourseIdAndEmployeeId(request.getCourseId(), request.getEmployeeId())
                .ifPresent(existing -> {
                    throw new BusinessException("Employee is already enrolled in this course");
                });
        DigitalEnrollment entity = new DigitalEnrollment();
        applyForm(entity, request);
        entity = digitalEnrollmentRepository.save(entity);
        activityLogService.log(MODULE, "CREATE", "DigitalEnrollment", entity.getId(), String.valueOf(entity.getId()), "Enrolled employee");
        return toDisplay(entity);
    }

    @Transactional
    public DigitalEnrollmentDisplayDto update(Long id, DigitalEnrollmentFormDto request) {
        DigitalEnrollment entity = load(id);
        applyForm(entity, request);
        entity = digitalEnrollmentRepository.save(entity);
        activityLogService.log(MODULE, "UPDATE", "DigitalEnrollment", entity.getId(), String.valueOf(entity.getId()), "Updated enrollment");
        return toDisplay(entity);
    }

    @Transactional
    public DigitalEnrollmentDisplayDto updateProgress(Long id, BigDecimal progressPct, BigDecimal score) {
        DigitalEnrollment entity = load(id);
        if (progressPct != null) {
            entity.setProgressPct(progressPct);
        }
        if (score != null) {
            entity.setScore(score);
        }
        if (entity.getProgressPct() != null && entity.getProgressPct().compareTo(new BigDecimal("100")) >= 0) {
            entity.setStatus("COMPLETED");
            entity.setCompletedAt(Instant.now());
            if (entity.getCertificateNo() == null || entity.getCertificateNo().isBlank()) {
                entity.setCertificateNo("CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }
        }
        entity = digitalEnrollmentRepository.save(entity);
        return toDisplay(entity);
    }

    @Transactional
    public void delete(Long id) {
        DigitalEnrollment entity = load(id);
        digitalEnrollmentRepository.delete(entity);
        activityLogService.log(MODULE, "DELETE", "DigitalEnrollment", id, String.valueOf(id), "Deleted enrollment");
    }

    private DigitalEnrollment load(Long id) {
        return digitalEnrollmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("DigitalEnrollment", id));
    }

    private void applyForm(DigitalEnrollment entity, DigitalEnrollmentFormDto request) {
        entity.setCourseId(request.getCourseId());
        entity.setEmployeeId(request.getEmployeeId());
        if (request.getProgressPct() != null) {
            entity.setProgressPct(request.getProgressPct());
        }
        entity.setScore(request.getScore());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (entity.getProgressPct() != null && entity.getProgressPct().compareTo(new BigDecimal("100")) >= 0) {
            entity.setStatus("COMPLETED");
            entity.setCompletedAt(Instant.now());
            if (entity.getCertificateNo() == null || entity.getCertificateNo().isBlank()) {
                entity.setCertificateNo("CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            }
        }
    }

    private DigitalEnrollmentDisplayDto toDisplay(DigitalEnrollment entity) {
        return DigitalEnrollmentDisplayDto.builder()
                .id(entity.getId())
                .courseId(entity.getCourseId())
                .employeeId(entity.getEmployeeId())
                .progressPct(entity.getProgressPct())
                .score(entity.getScore())
                .status(entity.getStatus())
                .completedAt(entity.getCompletedAt())
                .certificateNo(entity.getCertificateNo())
                .build();
    }
}
