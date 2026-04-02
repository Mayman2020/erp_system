package com.erp.system.accounting.controller;

import com.erp.system.accounting.dto.display.BillDisplayDto;
import com.erp.system.accounting.dto.form.BillFormDto;
import com.erp.system.accounting.service.BillService;
import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.BillStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounting/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @GetMapping
    public ApiResponse<List<BillDisplayDto>> getBills(@RequestParam(required = false) BillStatus status,
                                                      @RequestParam(required = false) String search) {
        return ApiResponse.success(billService.getBills(status, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<BillDisplayDto> getBill(@PathVariable Long id) {
        return ApiResponse.success(billService.getBill(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BillDisplayDto> createBill(@Valid @RequestBody BillFormDto request) {
        return ApiResponse.success(billService.createBill(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BillDisplayDto> updateBill(@PathVariable Long id, @Valid @RequestBody BillFormDto request) {
        return ApiResponse.success(billService.updateBill(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<BillDisplayDto> approveBill(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(billService.approveBill(id, actor));
    }

    @PostMapping("/{id}/post")
    public ApiResponse<BillDisplayDto> postBill(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(billService.postBill(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<BillDisplayDto> cancelBill(@PathVariable Long id,
                                                  @RequestParam String actor,
                                                  @RequestParam(required = false) String reason) {
        return ApiResponse.success(billService.cancelBill(id, actor, reason));
    }
}
