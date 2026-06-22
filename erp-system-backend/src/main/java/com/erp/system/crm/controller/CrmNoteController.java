package com.erp.system.crm.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.crm.dto.display.CrmNoteDisplayDto;
import com.erp.system.crm.dto.form.CrmNoteFormDto;
import com.erp.system.crm.service.CrmNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crm/notes")
@RequiredArgsConstructor
public class CrmNoteController {

    private final CrmNoteService crmNoteService;

    @GetMapping
    public ApiResponse<List<CrmNoteDisplayDto>> getAll() {
        return ApiResponse.success(crmNoteService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<CrmNoteDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(crmNoteService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CrmNoteDisplayDto> create(@Valid @RequestBody CrmNoteFormDto request) {
        return ApiResponse.success(crmNoteService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CrmNoteDisplayDto> update(@PathVariable Long id, @Valid @RequestBody CrmNoteFormDto request) {
        return ApiResponse.success(crmNoteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        crmNoteService.delete(id);
        return ApiResponse.success(null);
    }

}
