package com.erp.system.hr.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.dto.display.LeaveRequestDisplayDto;
import com.erp.system.hr.dto.form.LeaveRequestFormDto;
import com.erp.system.hr.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping
    public ApiResponse<List<LeaveRequestDisplayDto>> getAll() {
        return ApiResponse.success(leaveRequestService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<LeaveRequestDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(leaveRequestService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LeaveRequestDisplayDto> create(@Valid @RequestBody LeaveRequestFormDto request) {
        return ApiResponse.success(leaveRequestService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<LeaveRequestDisplayDto> update(@PathVariable Long id, @Valid @RequestBody LeaveRequestFormDto request) {
        return ApiResponse.success(leaveRequestService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<LeaveRequestDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(leaveRequestService.approve(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<LeaveRequestDisplayDto> cancel(@PathVariable Long id,
                                                      @RequestParam String actor,
                                                      @RequestParam(required = false) String reason) {
        return ApiResponse.success(leaveRequestService.cancel(id, actor, reason));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        leaveRequestService.delete(id);
        return ApiResponse.success(null);
    }
}
