package com.erp.system.maintenance.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.domain.Employee;
import com.erp.system.hr.repository.EmployeeRepository;
import com.erp.system.maintenance.domain.MaintenanceTechnician;
import com.erp.system.maintenance.dto.display.MaintenanceTechnicianDisplayDto;
import com.erp.system.maintenance.dto.form.MaintenanceTechnicianFormDto;
import com.erp.system.maintenance.repository.MaintenanceTechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceTechnicianService {

    private static final String MODULE = "MAINTENANCE";

    private final MaintenanceTechnicianRepository technicianRepository;
    private final EmployeeRepository employeeRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<MaintenanceTechnicianDisplayDto> getAll(Boolean activeOnly) {
        List<MaintenanceTechnician> technicians = Boolean.TRUE.equals(activeOnly)
                ? technicianRepository.findByActiveTrueOrderByDisplayNameAsc()
                : technicianRepository.findAllByOrderByDisplayNameAsc();
        return technicians.stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceTechnicianDisplayDto getById(Long id) {
        return toDisplay(load(id));
    }

    @Transactional
    public MaintenanceTechnicianDisplayDto create(MaintenanceTechnicianFormDto request) {
        MaintenanceTechnician technician = new MaintenanceTechnician();
        applyForm(technician, request);
        technician = technicianRepository.save(technician);
        activityLogService.log(MODULE, "CREATE", "MaintenanceTechnician", technician.getId(), technician.getDisplayName(),
                "Created maintenance technician " + technician.getDisplayName());
        return toDisplay(technician);
    }

    @Transactional
    public MaintenanceTechnicianDisplayDto update(Long id, MaintenanceTechnicianFormDto request) {
        MaintenanceTechnician technician = load(id);
        applyForm(technician, request);
        technician = technicianRepository.save(technician);
        activityLogService.log(MODULE, "UPDATE", "MaintenanceTechnician", technician.getId(), technician.getDisplayName(),
                "Updated maintenance technician " + technician.getDisplayName());
        return toDisplay(technician);
    }

    @Transactional
    public void delete(Long id) {
        MaintenanceTechnician technician = load(id);
        technicianRepository.delete(technician);
        activityLogService.log(MODULE, "DELETE", "MaintenanceTechnician", id, technician.getDisplayName(),
                "Deleted maintenance technician " + technician.getDisplayName());
    }

    MaintenanceTechnician load(Long id) {
        return technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceTechnician", id));
    }

    private void applyForm(MaintenanceTechnician technician, MaintenanceTechnicianFormDto request) {
        if (request.getEmployeeId() != null) {
            employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException("Employee not found"));
            technician.setEmployeeId(request.getEmployeeId());
        } else {
            technician.setEmployeeId(null);
        }
        technician.setDisplayName(request.getDisplayName().trim());
        technician.setSkillsCsv(trimToNull(request.getSkillsCsv()));
        if (request.getActive() != null) {
            technician.setActive(request.getActive());
        }
    }

    private MaintenanceTechnicianDisplayDto toDisplay(MaintenanceTechnician technician) {
        Employee employee = technician.getEmployeeId() == null ? null
                : employeeRepository.findById(technician.getEmployeeId()).orElse(null);
        return MaintenanceTechnicianDisplayDto.builder()
                .id(technician.getId())
                .employeeId(technician.getEmployeeId())
                .employeeName(employee != null ? employee.getFullNameEn() : null)
                .displayName(technician.getDisplayName())
                .skillsCsv(technician.getSkillsCsv())
                .active(technician.isActive())
                .createdAt(technician.getCreatedAt())
                .updatedAt(technician.getUpdatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
