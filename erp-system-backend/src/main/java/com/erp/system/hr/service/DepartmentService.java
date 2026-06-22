package com.erp.system.hr.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.domain.Department;
import com.erp.system.hr.dto.display.DepartmentDisplayDto;
import com.erp.system.hr.dto.form.DepartmentFormDto;
import com.erp.system.hr.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DepartmentService {

    private static final String MODULE = "HR";

    private final DepartmentRepository departmentRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<DepartmentDisplayDto> getAll() {
        return departmentRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentDisplayDto getById(Long id) {
        return toDisplay(loadDepartment(id));
    }

    @Transactional
    public DepartmentDisplayDto create(DepartmentFormDto request) {
        Department department = new Department();
        applyForm(department, request);
        department = departmentRepository.save(department);
        activityLogService.log(MODULE, "CREATE", "Department", department.getId(), String.valueOf(department.getId()),
                "Created Department " + department.getId());
        return toDisplay(department);
    }

    @Transactional
    public DepartmentDisplayDto update(Long id, DepartmentFormDto request) {
        Department department = loadDepartment(id);
        applyForm(department, request);
        department = departmentRepository.save(department);
        activityLogService.log(MODULE, "UPDATE", "Department", department.getId(), String.valueOf(department.getId()),
                "Updated Department " + department.getId());
        return toDisplay(department);
    }

    @Transactional
    public void delete(Long id) {
        Department department = loadDepartment(id);
        departmentRepository.delete(department);
        activityLogService.log(MODULE, "DELETE", "Department", id, String.valueOf(id),
                "Deleted Department " + id);
    }

    private Department loadDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    private void applyForm(Department department, DepartmentFormDto request) {

        department.setCode(request.getCode().trim());
        department.setNameEn(request.getNameEn().trim());
        department.setNameAr(request.getNameAr());
        department.setManagerId(request.getManagerId());
        department.setActive(request.getActive() == null || request.getActive());

    }

    private DepartmentDisplayDto toDisplay(Department department) {
        return DepartmentDisplayDto.builder()
                .id(department.getId())

                .code(department.getCode())
                .nameEn(department.getNameEn())
                .nameAr(department.getNameAr())
                .managerId(department.getManagerId())
                .active(department.isActive())

                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

}
