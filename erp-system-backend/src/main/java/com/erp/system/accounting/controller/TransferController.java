package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.TransferDisplayDto;
import com.erp.system.accounting.dto.form.TransferFormDto;
import com.erp.system.accounting.service.TransferService;
import com.erp.system.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @GetMapping
    public ApiResponse<List<TransferDisplayDto>> getAll() {
        return ApiResponse.success(transferService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<TransferDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(transferService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransferDisplayDto> create(@Valid @RequestBody TransferFormDto request) {
        return ApiResponse.success(transferService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransferDisplayDto> update(@PathVariable Long id, @Valid @RequestBody TransferFormDto request) {
        return ApiResponse.success(transferService.update(id, request));
    }

    @PostMapping("/{id}/post")
    public ApiResponse<TransferDisplayDto> post(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(transferService.post(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<TransferDisplayDto> cancel(@PathVariable Long id,
                                                  @RequestParam String actor,
                                                  @RequestParam(required = false) String reason) {
        return ApiResponse.success(transferService.cancel(id, actor, reason));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        transferService.delete(id);
        return ApiResponse.success(null);
    }
}
