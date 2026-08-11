package com.erp.system.hr.recruitment.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.recruitment.dto.display.HrCandidateDisplayDto;
import com.erp.system.hr.recruitment.dto.form.HrCandidateFormDto;
import com.erp.system.hr.recruitment.service.HrCandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/recruitment/candidates")
@RequiredArgsConstructor
public class HrCandidateController {

    private final HrCandidateService hrCandidateService;

    @GetMapping
    public ApiResponse<List<HrCandidateDisplayDto>> getAll(@RequestParam(required = false) Long vacancyId) {
        return ApiResponse.success(hrCandidateService.getAll(vacancyId));
    }

    @GetMapping("/{id}")
    public ApiResponse<HrCandidateDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(hrCandidateService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HrCandidateDisplayDto> create(@Valid @RequestBody HrCandidateFormDto request) {
        return ApiResponse.success(hrCandidateService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HrCandidateDisplayDto> update(@PathVariable Long id, @Valid @RequestBody HrCandidateFormDto request) {
        return ApiResponse.success(hrCandidateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        hrCandidateService.delete(id);
        return ApiResponse.success(null);
    }
}
