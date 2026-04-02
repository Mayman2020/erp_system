package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.ReconciliationDisplayDto;
import com.erp.system.accounting.dto.display.ReconciliationBankAccountDto;
import com.erp.system.accounting.dto.display.ReconciliationLineDisplayDto;
import com.erp.system.accounting.dto.display.ReconciliationMatchPairDisplayDto;
import com.erp.system.accounting.dto.display.ReconciliationSummaryDto;
import com.erp.system.accounting.dto.form.ReconciliationFormDto;
import com.erp.system.accounting.service.ReconciliationService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.ReconciliationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @GetMapping
    public ApiResponse<List<ReconciliationDisplayDto>> getReconciliations(
            @RequestParam(required = false) ReconciliationStatus status) {
        return ApiResponse.success(reconciliationService.getReconciliations(status));
    }

    @GetMapping("/bank-accounts")
    public ApiResponse<List<ReconciliationBankAccountDto>> getBankAccounts() {
        return ApiResponse.success(reconciliationService.getReconciliationBankAccounts());
    }

    @GetMapping("/{id}")
    public ApiResponse<ReconciliationDisplayDto> getReconciliation(@PathVariable Long id) {
        return ApiResponse.success(reconciliationService.getReconciliation(id));
    }

    @GetMapping("/{id}/statement-lines")
    public ApiResponse<List<ReconciliationLineDisplayDto>> getStatementLines(@PathVariable Long id) {
        return ApiResponse.success(reconciliationService.getStatementLines(id));
    }

    @GetMapping("/{id}/system-transactions")
    public ApiResponse<List<ReconciliationLineDisplayDto>> getSystemTransactions(@PathVariable Long id) {
        return ApiResponse.success(reconciliationService.getSystemTransactions(id));
    }

    @GetMapping("/{id}/summary")
    public ApiResponse<ReconciliationSummaryDto> getSummary(@PathVariable Long id) {
        return ApiResponse.success(reconciliationService.getSummary(id));
    }

    @GetMapping("/{id}/match-history")
    public ApiResponse<List<ReconciliationMatchPairDisplayDto>> getMatchHistory(@PathVariable Long id) {
        return ApiResponse.success(reconciliationService.getMatchHistory(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReconciliationDisplayDto> createReconciliation(
            @Valid @RequestBody ReconciliationFormDto request) {
        return ApiResponse.success(reconciliationService.createReconciliation(request));
    }

    @PostMapping("/{id}/match")
    public ApiResponse<ReconciliationDisplayDto> matchLines(
            @PathVariable Long id,
            @RequestParam Long statementLineId,
            @RequestParam Long systemLineId,
            @RequestParam(required = false) String actor) {
        return ApiResponse.success(reconciliationService.matchLines(id, statementLineId, systemLineId, actor));
    }

    @PostMapping("/{id}/unmatch")
    public ApiResponse<ReconciliationDisplayDto> unmatchLine(
            @PathVariable Long id,
            @RequestParam Long lineId,
            @RequestParam(required = false) String actor) {
        return ApiResponse.success(reconciliationService.unmatchLine(id, lineId, actor));
    }

    @PostMapping("/{id}/unmatch-pair")
    public ApiResponse<ReconciliationDisplayDto> unmatchPair(
            @PathVariable Long id,
            @RequestParam Long matchPairId,
            @RequestParam(required = false) String actor) {
        return ApiResponse.success(reconciliationService.unmatchPair(id, matchPairId, actor));
    }

    @PostMapping("/{id}/finalize")
    public ApiResponse<ReconciliationDisplayDto> finalizeReconciliation(
            @PathVariable Long id,
            @RequestParam String actor) {
        return ApiResponse.success(reconciliationService.finalizeReconciliation(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ReconciliationDisplayDto> cancelReconciliation(
            @PathVariable Long id,
            @RequestParam String actor) {
        return ApiResponse.success(reconciliationService.cancelReconciliation(id, actor));
    }
}
