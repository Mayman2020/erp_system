package com.erp.system.hr.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.domain.PayrollLine;
import com.erp.system.hr.dto.display.PayrollLineDisplayDto;
import com.erp.system.hr.dto.form.PayrollLineFormDto;
import com.erp.system.hr.repository.PayrollLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PayrollLineService {

    private static final String MODULE = "HR";

    private final PayrollLineRepository payrollLineRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PayrollLineDisplayDto> getAll() {
        return payrollLineRepository.findAllByOrderByIdDesc().stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public PayrollLineDisplayDto getById(Long id) {
        return toDisplay(loadPayrollLine(id));
    }

    @Transactional
    public PayrollLineDisplayDto create(PayrollLineFormDto request) {
        PayrollLine payrollLine = new PayrollLine();
        applyForm(payrollLine, request);
        payrollLine = payrollLineRepository.save(payrollLine);
        activityLogService.log(MODULE, "CREATE", "PayrollLine", payrollLine.getId(), String.valueOf(payrollLine.getId()),
                "Created PayrollLine " + payrollLine.getId());
        return toDisplay(payrollLine);
    }

    @Transactional
    public PayrollLineDisplayDto update(Long id, PayrollLineFormDto request) {
        PayrollLine payrollLine = loadPayrollLine(id);
        applyForm(payrollLine, request);
        payrollLine = payrollLineRepository.save(payrollLine);
        activityLogService.log(MODULE, "UPDATE", "PayrollLine", payrollLine.getId(), String.valueOf(payrollLine.getId()),
                "Updated PayrollLine " + payrollLine.getId());
        return toDisplay(payrollLine);
    }

    @Transactional
    public void delete(Long id) {
        PayrollLine payrollLine = loadPayrollLine(id);
        payrollLineRepository.delete(payrollLine);
        activityLogService.log(MODULE, "DELETE", "PayrollLine", id, String.valueOf(id),
                "Deleted PayrollLine " + id);
    }

    private PayrollLine loadPayrollLine(Long id) {
        return payrollLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollLine", id));
    }

    private void applyForm(PayrollLine payrollLine, PayrollLineFormDto request) {

        payrollLine.setPayrollId(request.getPayrollId());
        payrollLine.setEmployeeId(request.getEmployeeId());
        payrollLine.setBasicSalary(request.getBasicSalary());
        payrollLine.setAllowances(request.getAllowances() == null ? BigDecimal.ZERO : request.getAllowances());
        payrollLine.setDeductions(request.getDeductions() == null ? BigDecimal.ZERO : request.getDeductions());
        payrollLine.setNetSalary(request.getNetSalary());

    }

    private PayrollLineDisplayDto toDisplay(PayrollLine payrollLine) {
        return PayrollLineDisplayDto.builder()
                .id(payrollLine.getId())

                .payrollId(payrollLine.getPayrollId())
                .employeeId(payrollLine.getEmployeeId())
                .basicSalary(payrollLine.getBasicSalary())
                .allowances(payrollLine.getAllowances())
                .deductions(payrollLine.getDeductions())
                .netSalary(payrollLine.getNetSalary())

                .createdAt(payrollLine.getCreatedAt())
                .updatedAt(payrollLine.getUpdatedAt())
                .build();
    }

}
