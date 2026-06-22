package com.erp.system.crm.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.crm.domain.CrmActivity;
import com.erp.system.crm.dto.display.CrmActivityDisplayDto;
import com.erp.system.crm.dto.form.CrmActivityFormDto;
import com.erp.system.crm.repository.CrmActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.erp.system.crm.domain.CrmActivityStatus;

@Service
@RequiredArgsConstructor
public class CrmActivityService {

    private static final String MODULE = "CRM";

    private final CrmActivityRepository crmActivityRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<CrmActivityDisplayDto> getAll() {
        return crmActivityRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public CrmActivityDisplayDto getById(Long id) {
        return toDisplay(loadCrmActivity(id));
    }

    @Transactional
    public CrmActivityDisplayDto create(CrmActivityFormDto request) {
        CrmActivity crmActivity = new CrmActivity();
        applyForm(crmActivity, request);
        crmActivity = crmActivityRepository.save(crmActivity);
        activityLogService.log(MODULE, "CREATE", "CrmActivity", crmActivity.getId(), String.valueOf(crmActivity.getId()),
                "Created CrmActivity " + crmActivity.getId());
        return toDisplay(crmActivity);
    }

    @Transactional
    public CrmActivityDisplayDto update(Long id, CrmActivityFormDto request) {
        CrmActivity crmActivity = loadCrmActivity(id);
        applyForm(crmActivity, request);
        crmActivity = crmActivityRepository.save(crmActivity);
        activityLogService.log(MODULE, "UPDATE", "CrmActivity", crmActivity.getId(), String.valueOf(crmActivity.getId()),
                "Updated CrmActivity " + crmActivity.getId());
        return toDisplay(crmActivity);
    }

    @Transactional
    public void delete(Long id) {
        CrmActivity crmActivity = loadCrmActivity(id);
        crmActivityRepository.delete(crmActivity);
        activityLogService.log(MODULE, "DELETE", "CrmActivity", id, String.valueOf(id),
                "Deleted CrmActivity " + id);
    }

    private CrmActivity loadCrmActivity(Long id) {
        return crmActivityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CrmActivity", id));
    }

    private void applyForm(CrmActivity crmActivity, CrmActivityFormDto request) {

        crmActivity.setActivityType(request.getActivityType().trim());
        crmActivity.setSubject(request.getSubject().trim());
        crmActivity.setCustomerId(request.getCustomerId());
        crmActivity.setLeadId(request.getLeadId());
        crmActivity.setActivityDate(request.getActivityDate());
        crmActivity.setStatus(request.getStatus());
        crmActivity.setNotes(request.getNotes());

    }

    private CrmActivityDisplayDto toDisplay(CrmActivity crmActivity) {
        return CrmActivityDisplayDto.builder()
                .id(crmActivity.getId())

                .activityType(crmActivity.getActivityType())
                .subject(crmActivity.getSubject())
                .customerId(crmActivity.getCustomerId())
                .leadId(crmActivity.getLeadId())
                .activityDate(crmActivity.getActivityDate())
                .status(crmActivity.getStatus())
                .notes(crmActivity.getNotes())

                .createdAt(crmActivity.getCreatedAt())
                .updatedAt(crmActivity.getUpdatedAt())
                .build();
    }

}
