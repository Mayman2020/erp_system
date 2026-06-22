package com.erp.system.hr.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.hr.domain.PayrollRun;
import com.erp.system.hr.dto.display.PayrollLineDisplayDto;
import com.erp.system.hr.dto.display.PayrollRunDisplayDto;
import com.erp.system.hr.dto.form.PayrollRunFormDto;
import com.erp.system.hr.repository.PayrollLineRepository;
import com.erp.system.hr.repository.PayrollRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollRunService {

    private static final String MODULE = "HR";

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollLineRepository payrollLineRepository;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PayrollRunDisplayDto> getAll() {
        return payrollRunRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PayrollRunDisplayDto getById(Long id) {
        return toDisplay(loadPayrollRun(id));
    }

    @Transactional
    public PayrollRunDisplayDto create(PayrollRunFormDto request) {
        PayrollRun payrollRun = new PayrollRun();
        applyForm(payrollRun, request);
        payrollRun.setPayrollNumber(resolveNumber(request.getPayrollNumber()));
        payrollRun.setStatus(TransactionStatus.DRAFT);
        payrollRun = payrollRunRepository.save(payrollRun);
        activityLogService.log(MODULE, "CREATE", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                "Created payroll run " + payrollRun.getPayrollNumber());
        return toDisplay(payrollRun);
    }

    @Transactional
    public PayrollRunDisplayDto update(Long id, PayrollRunFormDto request) {
        PayrollRun payrollRun = loadPayrollRun(id);
        if (payrollRun.getStatus() == TransactionStatus.APPROVED) {
            throw new BusinessException("Approved payroll run cannot be edited");
        }
        applyForm(payrollRun, request);
        payrollRun = payrollRunRepository.save(payrollRun);
        activityLogService.log(MODULE, "UPDATE", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                "Updated payroll run " + payrollRun.getPayrollNumber());
        return toDisplay(payrollRun);
    }

    @Transactional
    public PayrollRunDisplayDto approve(Long id, String actor) {
        PayrollRun payrollRun = loadPayrollRun(id);
        if (payrollRun.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled payroll run cannot be approved");
        }
        payrollRun.setStatus(TransactionStatus.APPROVED);
        payrollRun = payrollRunRepository.save(payrollRun);
        activityLogService.log(MODULE, "APPROVE", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                "Approved payroll run " + payrollRun.getPayrollNumber());
        return toDisplay(payrollRun);
    }

    @Transactional
    public PayrollRunDisplayDto cancel(Long id, String actor, String reason) {
        PayrollRun payrollRun = loadPayrollRun(id);
        payrollRun.setStatus(TransactionStatus.CANCELLED);
        payrollRun = payrollRunRepository.save(payrollRun);
        activityLogService.log(MODULE, "CANCEL", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                "Cancelled payroll run " + payrollRun.getPayrollNumber());
        return toDisplay(payrollRun);
    }

    @Transactional
    public void delete(Long id) {
        PayrollRun payrollRun = loadPayrollRun(id);
        if (payrollRun.getStatus() == TransactionStatus.APPROVED) {
            throw new BusinessException("Approved payroll run cannot be deleted");
        }
        payrollRunRepository.delete(payrollRun);
        activityLogService.log(MODULE, "DELETE", "PayrollRun", id, payrollRun.getPayrollNumber(),
                "Deleted payroll run " + payrollRun.getPayrollNumber());
    }

    private String resolveNumber(String requested) {
        String normalized = requested == null ? null : requested.trim();
        if (normalized != null && !normalized.isEmpty()) {
            if (payrollRunRepository.existsByPayrollNumberIgnoreCase(normalized)) {
                throw new BusinessException("Payroll number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("PAYROLL_RUN");
        } catch (Exception exception) {
            return "PAY-" + System.currentTimeMillis();
        }
    }

    private void applyForm(PayrollRun payrollRun, PayrollRunFormDto request) {
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new BusinessException("Payroll period end cannot be before start");
        }
        payrollRun.setPeriodStart(request.getPeriodStart());
        payrollRun.setPeriodEnd(request.getPeriodEnd());
        payrollRun.setTotalAmount(request.getTotalAmount());
        payrollRun.setNotes(request.getNotes());
    }

    private PayrollRun loadPayrollRun(Long id) {
        return payrollRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", id));
    }

    private PayrollRunDisplayDto toDisplay(PayrollRun payrollRun) {
        List<PayrollLineDisplayDto> lines = payrollLineRepository.findByPayrollIdOrderByIdAsc(payrollRun.getId()).stream()
                .map(line -> PayrollLineDisplayDto.builder()
                        .id(line.getId())
                        .payrollId(line.getPayrollId())
                        .employeeId(line.getEmployeeId())
                        .basicSalary(line.getBasicSalary())
                        .allowances(line.getAllowances())
                        .deductions(line.getDeductions())
                        .netSalary(line.getNetSalary())
                        .createdAt(line.getCreatedAt())
                        .updatedAt(line.getUpdatedAt())
                        .build())
                .toList();
        return PayrollRunDisplayDto.builder()
                .id(payrollRun.getId())
                .payrollNumber(payrollRun.getPayrollNumber())
                .periodStart(payrollRun.getPeriodStart())
                .periodEnd(payrollRun.getPeriodEnd())
                .status(payrollRun.getStatus())
                .totalAmount(payrollRun.getTotalAmount())
                .notes(payrollRun.getNotes())
                .lines(lines)
                .createdAt(payrollRun.getCreatedAt())
                .updatedAt(payrollRun.getUpdatedAt())
                .build();
    }
}
