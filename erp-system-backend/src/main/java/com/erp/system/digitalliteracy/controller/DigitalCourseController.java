package com.erp.system.digitalliteracy.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.digitalliteracy.dto.display.DigitalCourseDisplayDto;
import com.erp.system.digitalliteracy.dto.form.DigitalCourseFormDto;
import com.erp.system.digitalliteracy.service.DigitalCourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/digital-literacy/courses")
@RequiredArgsConstructor
public class DigitalCourseController {

    private final DigitalCourseService digitalCourseService;

    @GetMapping
    public ApiResponse<List<DigitalCourseDisplayDto>> getAll() {
        return ApiResponse.success(digitalCourseService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<DigitalCourseDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(digitalCourseService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DigitalCourseDisplayDto> create(@Valid @RequestBody DigitalCourseFormDto request) {
        return ApiResponse.success(digitalCourseService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DigitalCourseDisplayDto> update(@PathVariable Long id, @Valid @RequestBody DigitalCourseFormDto request) {
        return ApiResponse.success(digitalCourseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        digitalCourseService.delete(id);
        return ApiResponse.success(null);
    }
}
