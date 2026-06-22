package com.erp.system.hr.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.dto.display.AttendanceRecordDisplayDto;
import com.erp.system.hr.dto.form.AttendanceRecordFormDto;
import com.erp.system.hr.service.AttendanceRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/attendance")
@RequiredArgsConstructor
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;

    @GetMapping
    public ApiResponse<List<AttendanceRecordDisplayDto>> getAll() {
        return ApiResponse.success(attendanceRecordService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<AttendanceRecordDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(attendanceRecordService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AttendanceRecordDisplayDto> create(@Valid @RequestBody AttendanceRecordFormDto request) {
        return ApiResponse.success(attendanceRecordService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AttendanceRecordDisplayDto> update(@PathVariable Long id, @Valid @RequestBody AttendanceRecordFormDto request) {
        return ApiResponse.success(attendanceRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        attendanceRecordService.delete(id);
        return ApiResponse.success(null);
    }

}
