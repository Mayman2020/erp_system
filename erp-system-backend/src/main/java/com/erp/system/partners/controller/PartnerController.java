package com.erp.system.partners.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.partners.dto.display.PartnerDisplayDto;
import com.erp.system.partners.dto.form.PartnerFormDto;
import com.erp.system.partners.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping
    public ApiResponse<List<PartnerDisplayDto>> getAll() {
        return ApiResponse.success(partnerService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PartnerDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(partnerService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PartnerDisplayDto> create(@Valid @RequestBody PartnerFormDto request) {
        return ApiResponse.success(partnerService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PartnerDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PartnerFormDto request) {
        return ApiResponse.success(partnerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        partnerService.delete(id);
        return ApiResponse.success(null);
    }
}
