package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.TransferDisplayDto;
import com.erp.system.accounting.dto.form.TransferFormDto;
import com.erp.system.accounting.service.TransferService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.TransferStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accounting/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @GetMapping
    public ApiResponse<List<TransferDisplayDto>> getTransfers(
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(transferService.getTransfers(status, fromDate, toDate, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransferDisplayDto> getTransfer(@PathVariable Long id) {
        return ApiResponse.success(transferService.getTransfer(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransferDisplayDto> createTransfer(@Valid @RequestBody TransferFormDto request) {
        return ApiResponse.success(transferService.createTransfer(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransferDisplayDto> updateTransfer(@PathVariable Long id, @Valid @RequestBody TransferFormDto request) {
        return ApiResponse.success(transferService.updateTransfer(id, request));
    }

    @PostMapping("/{id}/post")
    public ApiResponse<TransferDisplayDto> postTransfer(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(transferService.postTransfer(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<TransferDisplayDto> cancelTransfer(@PathVariable Long id,
                                                          @RequestParam String actor,
                                                          @RequestParam(required = false) String reason) {
        return ApiResponse.success(transferService.cancelTransfer(id, actor, reason));
    }
}
