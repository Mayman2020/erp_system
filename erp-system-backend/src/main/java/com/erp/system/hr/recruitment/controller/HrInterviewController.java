package com.erp.system.hr.recruitment.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.recruitment.dto.display.HrInterviewDisplayDto;
import com.erp.system.hr.recruitment.dto.form.HrInterviewFormDto;
import com.erp.system.hr.recruitment.service.HrInterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/recruitment/interviews")
@RequiredArgsConstructor
public class HrInterviewController {

    private final HrInterviewService hrInterviewService;

    @GetMapping
    public ApiResponse<List<HrInterviewDisplayDto>> getAll(@RequestParam(required = false) Long candidateId) {
        return ApiResponse.success(hrInterviewService.getAll(candidateId));
    }

    @GetMapping("/{id}")
    public ApiResponse<HrInterviewDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(hrInterviewService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HrInterviewDisplayDto> create(@Valid @RequestBody HrInterviewFormDto request) {
        return ApiResponse.success(hrInterviewService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HrInterviewDisplayDto> update(@PathVariable Long id, @Valid @RequestBody HrInterviewFormDto request) {
        return ApiResponse.success(hrInterviewService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        hrInterviewService.delete(id);
        return ApiResponse.success(null);
    }
}
