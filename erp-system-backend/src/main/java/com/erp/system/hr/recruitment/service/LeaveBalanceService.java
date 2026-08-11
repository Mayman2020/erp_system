package com.erp.system.hr.recruitment.service;

import com.erp.system.hr.recruitment.dto.display.LeaveBalanceDisplayDto;
import com.erp.system.hr.recruitment.repository.LeaveBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;

    @Transactional(readOnly = true)
    public List<LeaveBalanceDisplayDto> getAll(Long employeeId, Integer year) {
        var rows = employeeId != null
                ? leaveBalanceRepository.findByEmployeeIdOrderByYearDesc(employeeId)
                : year != null
                ? leaveBalanceRepository.findByYearOrderByEmployeeIdAsc(year)
                : leaveBalanceRepository.findAllByOrderByYearDescEmployeeIdAsc();
        return rows.stream().map(row -> LeaveBalanceDisplayDto.builder()
                .id(row.getId())
                .employeeId(row.getEmployeeId())
                .leaveType(row.getLeaveType())
                .balanceDays(row.getBalanceDays())
                .year(row.getYear())
                .build()).toList();
    }
}
