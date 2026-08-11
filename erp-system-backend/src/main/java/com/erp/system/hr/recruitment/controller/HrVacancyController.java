package com.erp.system.hr.recruitment.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.hr.recruitment.dto.display.HrVacancyDisplayDto;
import com.erp.system.hr.recruitment.dto.form.HrVacancyFormDto;
import com.erp.system.hr.recruitment.service.HrVacancyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/recruitment/vacancies")
@RequiredArgsConstructor
public class HrVacancyController {

    private final HrVacancyService hrVacancyService;

    @GetMapping
    public ApiResponse<List<HrVacancyDisplayDto>> getAll() {
        return ApiResponse.success(hrVacancyService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<HrVacancyDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(hrVacancyService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HrVacancyDisplayDto> create(@Valid @RequestBody HrVacancyFormDto request) {
        return ApiResponse.success(hrVacancyService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HrVacancyDisplayDto> update(@PathVariable Long id, @Valid @RequestBody HrVacancyFormDto request) {
        return ApiResponse.success(hrVacancyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        hrVacancyService.delete(id);
        return ApiResponse.success(null);
    }
}
