package com.erp.system.hr.recruitment.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.recruitment.dto.display.LeaveBalanceDisplayDto;
import com.erp.system.hr.recruitment.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/leave-balances")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @GetMapping
    public ApiResponse<List<LeaveBalanceDisplayDto>> getAll(@RequestParam(required = false) Long employeeId,
                                                              @RequestParam(required = false) Integer year) {
        return ApiResponse.success(leaveBalanceService.getAll(employeeId, year));
    }
}
