package com.erp.system.partners.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.partners.dto.display.PartnerTransactionDisplayDto;
import com.erp.system.partners.dto.form.PartnerTransactionFormDto;
import com.erp.system.partners.service.PartnerTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/partners/transactions")
@RequiredArgsConstructor
public class PartnerTransactionController {

    private final PartnerTransactionService partnerTransactionService;

    @GetMapping
    public ApiResponse<List<PartnerTransactionDisplayDto>> getAll(
            @RequestParam(required = false) Long partnerId) {
        return ApiResponse.success(partnerTransactionService.getAll(partnerId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PartnerTransactionDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(partnerTransactionService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PartnerTransactionDisplayDto> create(@Valid @RequestBody PartnerTransactionFormDto request) {
        return ApiResponse.success(partnerTransactionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PartnerTransactionDisplayDto> update(@PathVariable Long id,
                                                            @Valid @RequestBody PartnerTransactionFormDto request) {
        return ApiResponse.success(partnerTransactionService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<PartnerTransactionDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(partnerTransactionService.approve(id, actor));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        partnerTransactionService.delete(id);
        return ApiResponse.success(null);
    }
}
