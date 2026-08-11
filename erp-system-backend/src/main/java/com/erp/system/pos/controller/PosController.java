package com.erp.system.pos.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.pos.dto.display.*;
import com.erp.system.pos.dto.form.*;
import com.erp.system.pos.service.PosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pos")
@RequiredArgsConstructor
public class PosController {

    private final PosService posService;

    @GetMapping("/terminals")
    public ApiResponse<List<PosTerminalDisplayDto>> terminals() {
        return ApiResponse.success(posService.listTerminals());
    }

    @GetMapping("/shifts")
    public ApiResponse<List<PosShiftDisplayDto>> shifts() {
        return ApiResponse.success(posService.listShifts());
    }

    @GetMapping("/shifts/current")
    public ApiResponse<PosShiftDisplayDto> currentShift() {
        return ApiResponse.success(posService.getOpenShiftForCurrentUser());
    }

    @PostMapping("/shifts/open")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PosShiftDisplayDto> openShift(@Valid @RequestBody PosOpenShiftFormDto request) {
        return ApiResponse.success(posService.openShift(request));
    }

    @PostMapping("/shifts/{id}/close")
    public ApiResponse<PosShiftDisplayDto> closeShift(@PathVariable Long id,
                                                      @Valid @RequestBody PosCloseShiftFormDto request) {
        return ApiResponse.success(posService.closeShift(id, request));
    }

    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PosSaleDisplayDto> createSale(@Valid @RequestBody PosSaleFormDto request) {
        return ApiResponse.success(posService.createSale(request));
    }

    @PostMapping("/offline/sync")
    public ApiResponse<PosOfflineSyncResultDto> syncOffline(@Valid @RequestBody PosOfflineSyncFormDto request) {
        return ApiResponse.success(posService.syncOffline(request));
    }
}
