package com.erp.system.inventory.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.inventory.dto.display.ReplenishmentProposalDisplayDto;
import com.erp.system.inventory.service.ReplenishmentService;
import com.erp.system.purchases.dto.display.PurchaseOrderDisplayDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/stock/replenishment")
@RequiredArgsConstructor
public class ReplenishmentController {

    private final ReplenishmentService replenishmentService;

    @GetMapping
    public ApiResponse<List<ReplenishmentProposalDisplayDto>> getAll(@RequestParam(required = false) String status) {
        return ApiResponse.success(replenishmentService.getAll(status));
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<List<ReplenishmentProposalDisplayDto>> generate() {
        return ApiResponse.success(replenishmentService.generate());
    }

    @PostMapping("/convert")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PurchaseOrderDisplayDto> convertToPurchaseOrder(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam Long supplierId
    ) {
        return ApiResponse.success(replenishmentService.convertToPurchaseOrder(warehouseId, supplierId));
    }
}
