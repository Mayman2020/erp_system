package com.erp.system.alerts.controller;

import com.erp.system.alerts.dto.display.AlertEventDisplayDto;
import com.erp.system.alerts.service.AlertEventService;
import com.erp.system.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertEventController {

    private final AlertEventService alertEventService;

    @GetMapping
    public ApiResponse<List<AlertEventDisplayDto>> getAll(@RequestParam(required = false) String status) {
        return ApiResponse.success(alertEventService.getAll(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<AlertEventDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(alertEventService.getById(id));
    }

    @PostMapping("/{id}/acknowledge")
    public ApiResponse<AlertEventDisplayDto> acknowledge(@PathVariable Long id) {
        return ApiResponse.success(alertEventService.acknowledge(id));
    }
}
