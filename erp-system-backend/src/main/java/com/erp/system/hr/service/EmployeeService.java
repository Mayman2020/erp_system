package com.erp.system.hr.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.domain.Employee;
import com.erp.system.hr.dto.display.EmployeeDisplayDto;
import com.erp.system.hr.dto.form.EmployeeFormDto;
import com.erp.system.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final String MODULE = "HR";

    private final EmployeeRepository employeeRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<EmployeeDisplayDto> getAll() {
        return employeeRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeDisplayDto getById(Long id) {
        return toDisplay(loadEmployee(id));
    }

    @Transactional
    public EmployeeDisplayDto create(EmployeeFormDto request) {
        Employee employee = new Employee();
        applyForm(employee, request);
        employee = employeeRepository.save(employee);
        activityLogService.log(MODULE, "CREATE", "Employee", employee.getId(), String.valueOf(employee.getId()),
                "Created Employee " + employee.getId());
        return toDisplay(employee);
    }

    @Transactional
    public EmployeeDisplayDto update(Long id, EmployeeFormDto request) {
        Employee employee = loadEmployee(id);
        applyForm(employee, request);
        employee = employeeRepository.save(employee);
        activityLogService.log(MODULE, "UPDATE", "Employee", employee.getId(), String.valueOf(employee.getId()),
                "Updated Employee " + employee.getId());
        return toDisplay(employee);
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = loadEmployee(id);
        employeeRepository.delete(employee);
        activityLogService.log(MODULE, "DELETE", "Employee", id, String.valueOf(id),
                "Deleted Employee " + id);
    }

    private Employee loadEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    private void applyForm(Employee employee, EmployeeFormDto request) {

        employee.setEmployeeCode(request.getEmployeeCode().trim());
        employee.setFullNameEn(request.getFullNameEn().trim());
        employee.setFullNameAr(request.getFullNameAr());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setDepartmentId(request.getDepartmentId());
        employee.setJobTitle(request.getJobTitle());
        employee.setHireDate(request.getHireDate());
        employee.setBasicSalary(request.getBasicSalary());
        employee.setActive(request.getActive() == null || request.getActive());

    }

    private EmployeeDisplayDto toDisplay(Employee employee) {
        return EmployeeDisplayDto.builder()
                .id(employee.getId())

                .employeeCode(employee.getEmployeeCode())
                .fullNameEn(employee.getFullNameEn())
                .fullNameAr(employee.getFullNameAr())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .departmentId(employee.getDepartmentId())
                .jobTitle(employee.getJobTitle())
                .hireDate(employee.getHireDate())
                .basicSalary(employee.getBasicSalary())
                .active(employee.isActive())

                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .createdBy(employee.getCreatedBy())
                .updatedBy(employee.getUpdatedBy())
                .build();
    }

}
