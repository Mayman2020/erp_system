package com.erp.system.inventory.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.inventory.dto.display.LowStockAlertDisplayDto;
import com.erp.system.inventory.dto.display.StockLevelDisplayDto;
import com.erp.system.inventory.dto.display.StockMovementDisplayDto;
import com.erp.system.inventory.dto.form.StockMovementFormDto;
import com.erp.system.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/inventory/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/levels")
    public ApiResponse<List<StockLevelDisplayDto>> getStockLevels(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId
    ) {
        return ApiResponse.success(stockService.getStockLevels(productId, warehouseId));
    }

    @GetMapping("/levels/{id}")
    public ApiResponse<StockLevelDisplayDto> getStockLevel(@PathVariable Long id) {
        return ApiResponse.success(stockService.getStockLevel(id));
    }

    @GetMapping("/low-stock")
    public ApiResponse<List<LowStockAlertDisplayDto>> getLowStockAlerts() {
        return ApiResponse.success(stockService.getLowStockAlerts());
    }

    @GetMapping("/movements")
    public ApiResponse<List<StockMovementDisplayDto>> getMovements(
            @RequestParam(required = false) StockMovementType movementType,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ApiResponse.success(stockService.getMovements(movementType, status, productId, warehouseId, search, fromDate, toDate));
    }

    @GetMapping("/movements/{id}")
    public ApiResponse<StockMovementDisplayDto> getMovement(@PathVariable Long id) {
        return ApiResponse.success(stockService.getMovement(id));
    }

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StockMovementDisplayDto> createMovement(@Valid @RequestBody StockMovementFormDto request) {
        return ApiResponse.success(stockService.createMovement(request));
    }

    @PutMapping("/movements/{id}")
    public ApiResponse<StockMovementDisplayDto> updateMovement(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementFormDto request
    ) {
        return ApiResponse.success(stockService.updateMovement(id, request));
    }

    @PutMapping("/movements/{id}/submit")
    public ApiResponse<StockMovementDisplayDto> submitMovement(@PathVariable Long id) {
        return ApiResponse.success(stockService.submitMovement(id));
    }

    @PutMapping("/movements/{id}/approve")
    public ApiResponse<StockMovementDisplayDto> approveMovement(@PathVariable Long id) {
        return ApiResponse.success(stockService.approveMovement(id));
    }

    @PutMapping("/movements/{id}/cancel")
    public ApiResponse<StockMovementDisplayDto> cancelMovement(@PathVariable Long id) {
        return ApiResponse.success(stockService.cancelMovement(id));
    }

    @PostMapping("/in")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StockMovementDisplayDto> stockIn(@Valid @RequestBody StockMovementFormDto request) {
        return ApiResponse.success(stockService.stockIn(request));
    }

    @PostMapping("/out")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StockMovementDisplayDto> stockOut(@Valid @RequestBody StockMovementFormDto request) {
        return ApiResponse.success(stockService.stockOut(request));
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StockMovementDisplayDto> transferStock(@Valid @RequestBody StockMovementFormDto request) {
        return ApiResponse.success(stockService.transferStock(request));
    }
}
