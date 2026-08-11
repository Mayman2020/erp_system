package com.erp.system.inventory.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.inventory.dto.display.StockIncidentDisplayDto;
import com.erp.system.inventory.dto.form.StockIncidentFormDto;
import com.erp.system.inventory.service.StockIncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory/stock/incidents")
@RequiredArgsConstructor
public class StockIncidentController {

    private final StockIncidentService incidentService;

    @GetMapping
    public ApiResponse<List<StockIncidentDisplayDto>> getAll(@RequestParam(required = false) String status) {
        return ApiResponse.success(incidentService.getAll(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<StockIncidentDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(incidentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StockIncidentDisplayDto> create(@Valid @RequestBody StockIncidentFormDto request) {
        return ApiResponse.success(incidentService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StockIncidentDisplayDto> update(
            @PathVariable Long id,
            @Valid @RequestBody StockIncidentFormDto request
    ) {
        return ApiResponse.success(incidentService.update(id, request));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<StockIncidentDisplayDto> approve(@PathVariable Long id) {
        return ApiResponse.success(incidentService.approve(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        incidentService.delete(id);
        return ApiResponse.success(null);
    }
}
