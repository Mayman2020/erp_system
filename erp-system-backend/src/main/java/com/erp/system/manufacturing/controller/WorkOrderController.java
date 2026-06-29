package com.erp.system.manufacturing.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.manufacturing.dto.display.WorkOrderDisplayDto;
import com.erp.system.manufacturing.dto.form.WorkOrderFormDto;
import com.erp.system.manufacturing.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manufacturing/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public ApiResponse<List<WorkOrderDisplayDto>> getAll() {
        return ApiResponse.success(workOrderService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkOrderDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(workOrderService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WorkOrderDisplayDto> create(@Valid @RequestBody WorkOrderFormDto request) {
        return ApiResponse.success(workOrderService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WorkOrderDisplayDto> update(@PathVariable Long id, @Valid @RequestBody WorkOrderFormDto request) {
        return ApiResponse.success(workOrderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        workOrderService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/start")
    public ApiResponse<WorkOrderDisplayDto> start(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(workOrderService.start(id, actor));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<WorkOrderDisplayDto> complete(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(workOrderService.complete(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<WorkOrderDisplayDto> cancel(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(workOrderService.cancel(id, actor));
    }
}
