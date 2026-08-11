package com.erp.system.digitalliteracy.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.digitalliteracy.dto.display.DigitalEnrollmentDisplayDto;
import com.erp.system.digitalliteracy.dto.form.DigitalEnrollmentFormDto;
import com.erp.system.digitalliteracy.service.DigitalEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/digital-literacy/enrollments")
@RequiredArgsConstructor
public class DigitalEnrollmentController {

    private final DigitalEnrollmentService digitalEnrollmentService;

    @GetMapping
    public ApiResponse<List<DigitalEnrollmentDisplayDto>> getAll(@RequestParam(required = false) Long courseId,
                                                                 @RequestParam(required = false) Long employeeId) {
        return ApiResponse.success(digitalEnrollmentService.getAll(courseId, employeeId));
    }

    @GetMapping("/{id}")
    public ApiResponse<DigitalEnrollmentDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(digitalEnrollmentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DigitalEnrollmentDisplayDto> create(@Valid @RequestBody DigitalEnrollmentFormDto request) {
        return ApiResponse.success(digitalEnrollmentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DigitalEnrollmentDisplayDto> update(@PathVariable Long id, @Valid @RequestBody DigitalEnrollmentFormDto request) {
        return ApiResponse.success(digitalEnrollmentService.update(id, request));
    }

    @PostMapping("/{id}/progress")
    public ApiResponse<DigitalEnrollmentDisplayDto> updateProgress(@PathVariable Long id,
                                                                   @RequestParam(required = false) BigDecimal progressPct,
                                                                   @RequestParam(required = false) BigDecimal score) {
        return ApiResponse.success(digitalEnrollmentService.updateProgress(id, progressPct, score));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        digitalEnrollmentService.delete(id);
        return ApiResponse.success(null);
    }
}
