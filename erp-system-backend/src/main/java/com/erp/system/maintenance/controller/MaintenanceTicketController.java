package com.erp.system.maintenance.controller;

import com.erp.system.common.dto.ApiResponse;
import com.erp.system.maintenance.dto.display.MaintenanceChecklistDisplayDto;
import com.erp.system.maintenance.dto.display.MaintenanceSparePartDisplayDto;
import com.erp.system.maintenance.dto.display.MaintenanceTicketDisplayDto;
import com.erp.system.maintenance.dto.form.AssignTechnicianFormDto;
import com.erp.system.maintenance.dto.form.MaintenanceChecklistFormDto;
import com.erp.system.maintenance.dto.form.MaintenanceSparePartFormDto;
import com.erp.system.maintenance.dto.form.MaintenanceTicketFormDto;
import com.erp.system.maintenance.service.MaintenanceTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenance/tickets")
@RequiredArgsConstructor
public class MaintenanceTicketController {

    private final MaintenanceTicketService ticketService;

    @GetMapping
    public ApiResponse<List<MaintenanceTicketDisplayDto>> getAll(@RequestParam(required = false) String status) {
        return ApiResponse.success(ticketService.getAll(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<MaintenanceTicketDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(ticketService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MaintenanceTicketDisplayDto> create(@Valid @RequestBody MaintenanceTicketFormDto request) {
        return ApiResponse.success(ticketService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<MaintenanceTicketDisplayDto> update(@PathVariable Long id, @Valid @RequestBody MaintenanceTicketFormDto request) {
        return ApiResponse.success(ticketService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ticketService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/assign")
    public ApiResponse<MaintenanceTicketDisplayDto> assign(@PathVariable Long id,
                                                           @Valid @RequestBody AssignTechnicianFormDto request,
                                                           @RequestParam String actor) {
        return ApiResponse.success(ticketService.assignTechnician(id, request, actor));
    }

    @PostMapping("/{id}/start")
    public ApiResponse<MaintenanceTicketDisplayDto> start(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(ticketService.start(id, actor));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<MaintenanceTicketDisplayDto> complete(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(ticketService.complete(id, actor));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<MaintenanceTicketDisplayDto> cancel(@PathVariable Long id, @RequestParam String actor) {
        return ApiResponse.success(ticketService.cancel(id, actor));
    }

    @PostMapping("/{id}/checklists")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MaintenanceChecklistDisplayDto> addChecklist(@PathVariable Long id,
                                                                    @Valid @RequestBody MaintenanceChecklistFormDto request) {
        return ApiResponse.success(ticketService.addChecklistItem(id, request));
    }

    @PutMapping("/{id}/checklists/{checklistId}")
    public ApiResponse<MaintenanceChecklistDisplayDto> updateChecklist(@PathVariable Long id,
                                                                         @PathVariable Long checklistId,
                                                                         @Valid @RequestBody MaintenanceChecklistFormDto request) {
        return ApiResponse.success(ticketService.updateChecklistItem(id, checklistId, request));
    }

    @DeleteMapping("/{id}/checklists/{checklistId}")
    public ApiResponse<Void> deleteChecklist(@PathVariable Long id, @PathVariable Long checklistId) {
        ticketService.deleteChecklistItem(id, checklistId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/spare-parts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MaintenanceSparePartDisplayDto> addSparePart(@PathVariable Long id,
                                                                    @Valid @RequestBody MaintenanceSparePartFormDto request) {
        return ApiResponse.success(ticketService.addSparePart(id, request));
    }

    @PostMapping("/{id}/spare-parts/{sparePartId}/issue")
    public ApiResponse<MaintenanceSparePartDisplayDto> issueSparePart(@PathVariable Long id,
                                                                        @PathVariable Long sparePartId,
                                                                        @RequestParam String actor) {
        return ApiResponse.success(ticketService.issueSparePart(id, sparePartId, actor));
    }

    @DeleteMapping("/{id}/spare-parts/{sparePartId}")
    public ApiResponse<Void> deleteSparePart(@PathVariable Long id, @PathVariable Long sparePartId) {
        ticketService.deleteSparePart(id, sparePartId);
        return ApiResponse.success(null);
    }
}
