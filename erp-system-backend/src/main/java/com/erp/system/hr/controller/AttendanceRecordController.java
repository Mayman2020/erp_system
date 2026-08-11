package com.erp.system.hr.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.dto.PageResponse;
import com.erp.system.hr.dto.display.AttendanceRecordDisplayDto;
import com.erp.system.hr.dto.form.AttendanceRecordFormDto;
import com.erp.system.hr.service.AttendanceRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/hr/attendance")
@RequiredArgsConstructor
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;

    @GetMapping
    public ApiResponse<List<AttendanceRecordDisplayDto>> getAll() {
        return ApiResponse.success(attendanceRecordService.getAll());
    }

    @GetMapping("/paged")
    public ApiResponse<PageResponse<AttendanceRecordDisplayDto>> getPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                Sort.by(Sort.Order.desc("attendanceDate"), Sort.Order.desc("id")));
        return ApiResponse.success(attendanceRecordService.getPaged(
                employeeId, status, q, fromDate, toDate, pageable));
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
