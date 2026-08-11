package com.erp.system.maintenance.service;

import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.Warehouse;
import com.erp.system.inventory.dto.display.StockMovementDisplayDto;
import com.erp.system.inventory.dto.form.StockMovementFormDto;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.WarehouseRepository;
import com.erp.system.inventory.service.StockService;
import com.erp.system.maintenance.domain.MaintenanceChecklist;
import com.erp.system.maintenance.domain.MaintenanceSparePart;
import com.erp.system.maintenance.domain.MaintenanceTicket;
import com.erp.system.maintenance.dto.display.MaintenanceChecklistDisplayDto;
import com.erp.system.maintenance.dto.display.MaintenanceSparePartDisplayDto;
import com.erp.system.maintenance.dto.display.MaintenanceTicketDisplayDto;
import com.erp.system.maintenance.dto.form.AssignTechnicianFormDto;
import com.erp.system.maintenance.dto.form.MaintenanceChecklistFormDto;
import com.erp.system.maintenance.dto.form.MaintenanceSparePartFormDto;
import com.erp.system.maintenance.dto.form.MaintenanceTicketFormDto;
import com.erp.system.maintenance.repository.MaintenanceChecklistRepository;
import com.erp.system.maintenance.repository.MaintenanceSparePartRepository;
import com.erp.system.maintenance.repository.MaintenanceTicketRepository;
import com.erp.system.sales.domain.Customer;
import com.erp.system.sales.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MaintenanceTicketService {

    private static final String MODULE = "MAINTENANCE";
    private static final String REFERENCE_TYPE = "MAINTENANCE_TICKET";
    private static final Set<String> EDITABLE_STATUSES = Set.of("OPEN", "ASSIGNED");

    private final MaintenanceTicketRepository ticketRepository;
    private final MaintenanceChecklistRepository checklistRepository;
    private final MaintenanceSparePartRepository sparePartRepository;
    private final MaintenanceAssetService assetService;
    private final MaintenanceTechnicianService technicianService;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockService stockService;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<MaintenanceTicketDisplayDto> getAll(String status) {
        return ticketRepository.findAllByOrderByOpenedAtDescIdDesc().stream()
                .filter(ticket -> !StringUtils.hasText(status) || status.equalsIgnoreCase(ticket.getStatus()))
                .map(this::toSummaryDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceTicketDisplayDto getById(Long id) {
        return toDetailDisplay(load(id));
    }

    @Transactional
    public MaintenanceTicketDisplayDto create(MaintenanceTicketFormDto request) {
        MaintenanceTicket ticket = new MaintenanceTicket();
        applyForm(ticket, request);
        ticket.setTicketNo(resolveTicketNo(request.getTicketNo()));
        ticket.setStatus("OPEN");
        ticket.setOpenedAt(LocalDateTime.now());
        ticket = ticketRepository.save(ticket);
        replaceChecklists(ticket.getId(), request.getChecklists());
        activityLogService.log(MODULE, "CREATE", "MaintenanceTicket", ticket.getId(), ticket.getTicketNo(),
                "Created maintenance ticket " + ticket.getTicketNo());
        return toDetailDisplay(ticket);
    }

    @Transactional
    public MaintenanceTicketDisplayDto update(Long id, MaintenanceTicketFormDto request) {
        MaintenanceTicket ticket = load(id);
        assertEditable(ticket);
        applyForm(ticket, request);
        ticket = ticketRepository.save(ticket);
        if (request.getChecklists() != null) {
            replaceChecklists(ticket.getId(), request.getChecklists());
        }
        activityLogService.log(MODULE, "UPDATE", "MaintenanceTicket", ticket.getId(), ticket.getTicketNo(),
                "Updated maintenance ticket " + ticket.getTicketNo());
        return toDetailDisplay(ticket);
    }

    @Transactional
    public void delete(Long id) {
        MaintenanceTicket ticket = load(id);
        if (!EDITABLE_STATUSES.contains(ticket.getStatus()) && !"CANCELLED".equals(ticket.getStatus())) {
            throw new BusinessException("Only open, assigned, or cancelled tickets can be deleted");
        }
        checklistRepository.deleteByTicketId(id);
        sparePartRepository.deleteByTicketId(id);
        ticketRepository.delete(ticket);
        activityLogService.log(MODULE, "DELETE", "MaintenanceTicket", id, ticket.getTicketNo(),
                "Deleted maintenance ticket " + ticket.getTicketNo());
    }

    @Transactional
    public MaintenanceTicketDisplayDto assignTechnician(Long id, AssignTechnicianFormDto request, String actor) {
        MaintenanceTicket ticket = load(id);
        if ("CLOSED".equals(ticket.getStatus()) || "CANCELLED".equals(ticket.getStatus())) {
            throw new BusinessException("Closed or cancelled tickets cannot be reassigned");
        }
        technicianService.load(request.getTechnicianId());
        ticket.setTechnicianId(request.getTechnicianId());
        if ("OPEN".equals(ticket.getStatus())) {
            ticket.setStatus("ASSIGNED");
        }
        ticket.setUpdatedBy(actor);
        ticket = ticketRepository.save(ticket);
        activityLogService.log(MODULE, "ASSIGN", "MaintenanceTicket", ticket.getId(), ticket.getTicketNo(),
                "Assigned technician to ticket " + ticket.getTicketNo());
        return toDetailDisplay(ticket);
    }

    @Transactional
    public MaintenanceTicketDisplayDto start(Long id, String actor) {
        MaintenanceTicket ticket = load(id);
        if (!"OPEN".equals(ticket.getStatus()) && !"ASSIGNED".equals(ticket.getStatus())) {
            throw new BusinessException("Only open or assigned tickets can be started");
        }
        ticket.setStatus("IN_PROGRESS");
        ticket.setUpdatedBy(actor);
        ticket = ticketRepository.save(ticket);
        activityLogService.log(MODULE, "START", "MaintenanceTicket", ticket.getId(), ticket.getTicketNo(),
                "Started maintenance ticket " + ticket.getTicketNo());
        return toDetailDisplay(ticket);
    }

    @Transactional
    public MaintenanceTicketDisplayDto complete(Long id, String actor) {
        MaintenanceTicket ticket = load(id);
        if (!"IN_PROGRESS".equals(ticket.getStatus()) && !"ASSIGNED".equals(ticket.getStatus())) {
            throw new BusinessException("Ticket cannot be completed from status " + ticket.getStatus());
        }
        issuePendingSpareParts(ticket);
        ticket.setStatus("CLOSED");
        ticket.setClosedAt(LocalDateTime.now());
        ticket.setUpdatedBy(actor);
        ticket = ticketRepository.save(ticket);
        activityLogService.log(MODULE, "COMPLETE", "MaintenanceTicket", ticket.getId(), ticket.getTicketNo(),
                "Completed maintenance ticket " + ticket.getTicketNo());
        return toDetailDisplay(ticket);
    }

    @Transactional
    public MaintenanceTicketDisplayDto cancel(Long id, String actor) {
        MaintenanceTicket ticket = load(id);
        if ("CLOSED".equals(ticket.getStatus())) {
            throw new BusinessException("Closed tickets cannot be cancelled");
        }
        ticket.setStatus("CANCELLED");
        ticket.setUpdatedBy(actor);
        ticket = ticketRepository.save(ticket);
        activityLogService.log(MODULE, "CANCEL", "MaintenanceTicket", ticket.getId(), ticket.getTicketNo(),
                "Cancelled maintenance ticket " + ticket.getTicketNo());
        return toDetailDisplay(ticket);
    }

    @Transactional
    public MaintenanceChecklistDisplayDto addChecklistItem(Long ticketId, MaintenanceChecklistFormDto request) {
        MaintenanceTicket ticket = load(ticketId);
        assertEditable(ticket);
        MaintenanceChecklist item = MaintenanceChecklist.builder()
                .ticketId(ticketId)
                .itemText(request.getItemText().trim())
                .done(Boolean.TRUE.equals(request.getDone()))
                .sortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder())
                .build();
        item = checklistRepository.save(item);
        return toChecklistDisplay(item);
    }

    @Transactional
    public MaintenanceChecklistDisplayDto updateChecklistItem(Long ticketId, Long checklistId, MaintenanceChecklistFormDto request) {
        MaintenanceTicket ticket = load(ticketId);
        assertEditable(ticket);
        MaintenanceChecklist item = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceChecklist", checklistId));
        if (!ticketId.equals(item.getTicketId())) {
            throw new BusinessException("Checklist item does not belong to this ticket");
        }
        item.setItemText(request.getItemText().trim());
        if (request.getDone() != null) {
            item.setDone(request.getDone());
        }
        if (request.getSortOrder() != null) {
            item.setSortOrder(request.getSortOrder());
        }
        item = checklistRepository.save(item);
        return toChecklistDisplay(item);
    }

    @Transactional
    public void deleteChecklistItem(Long ticketId, Long checklistId) {
        MaintenanceTicket ticket = load(ticketId);
        assertEditable(ticket);
        MaintenanceChecklist item = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceChecklist", checklistId));
        if (!ticketId.equals(item.getTicketId())) {
            throw new BusinessException("Checklist item does not belong to this ticket");
        }
        checklistRepository.delete(item);
    }

    @Transactional
    public MaintenanceSparePartDisplayDto addSparePart(Long ticketId, MaintenanceSparePartFormDto request) {
        MaintenanceTicket ticket = load(ticketId);
        assertEditable(ticket);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehouseId()));
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }
        BigDecimal unitCost = request.getUnitCost();
        if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) == 0) {
            unitCost = product.getCostPrice() == null ? BigDecimal.ZERO : product.getCostPrice();
        }
        MaintenanceSparePart sparePart = MaintenanceSparePart.builder()
                .ticketId(ticketId)
                .productId(request.getProductId())
                .warehouseId(request.getWarehouseId())
                .quantity(request.getQuantity())
                .unitCost(unitCost)
                .build();
        sparePart = sparePartRepository.save(sparePart);
        return toSparePartDisplay(sparePart);
    }

    @Transactional
    public MaintenanceSparePartDisplayDto issueSparePart(Long ticketId, Long sparePartId, String actor) {
        MaintenanceTicket ticket = load(ticketId);
        if ("CLOSED".equals(ticket.getStatus()) || "CANCELLED".equals(ticket.getStatus())) {
            throw new BusinessException("Spare parts cannot be issued on closed or cancelled tickets");
        }
        MaintenanceSparePart sparePart = loadSparePart(sparePartId, ticketId);
        issueSparePartStock(ticket, sparePart);
        sparePart = sparePartRepository.save(sparePart);
        ticket.setUpdatedBy(actor);
        ticketRepository.save(ticket);
        activityLogService.log(MODULE, "ISSUE_SPARE", "MaintenanceTicket", ticket.getId(), ticket.getTicketNo(),
                "Issued spare part for ticket " + ticket.getTicketNo());
        return toSparePartDisplay(sparePart);
    }

    @Transactional
    public void deleteSparePart(Long ticketId, Long sparePartId) {
        MaintenanceTicket ticket = load(ticketId);
        assertEditable(ticket);
        MaintenanceSparePart sparePart = loadSparePart(sparePartId, ticketId);
        if (sparePart.getMovementId() != null) {
            throw new BusinessException("Issued spare parts cannot be deleted");
        }
        sparePartRepository.delete(sparePart);
    }

    private void issuePendingSpareParts(MaintenanceTicket ticket) {
        sparePartRepository.findByTicketIdOrderByIdAsc(ticket.getId()).stream()
                .filter(part -> part.getMovementId() == null)
                .forEach(part -> {
                    issueSparePartStock(ticket, part);
                    sparePartRepository.save(part);
                });
    }

    private void issueSparePartStock(MaintenanceTicket ticket, MaintenanceSparePart sparePart) {
        if (sparePart.getMovementId() != null) {
            return;
        }
        StockMovementDisplayDto movement = createStockMovement(ticket, sparePart);
        sparePart.setMovementId(movement.getId());
    }

    private StockMovementDisplayDto createStockMovement(MaintenanceTicket ticket, MaintenanceSparePart sparePart) {
        StockMovementFormDto form = new StockMovementFormDto();
        form.setMovementDate(LocalDate.now());
        form.setMovementType(StockMovementType.OUT);
        form.setProductId(sparePart.getProductId());
        form.setWarehouseId(sparePart.getWarehouseId());
        form.setQuantity(sparePart.getQuantity());
        form.setUnitCost(sparePart.getUnitCost());
        form.setReferenceType(REFERENCE_TYPE);
        form.setReferenceId(ticket.getId());
        form.setNotes("Spare parts for ticket " + ticket.getTicketNo());
        form.setApproveImmediately(true);
        return stockService.stockOut(form);
    }

    private void replaceChecklists(Long ticketId, List<MaintenanceChecklistFormDto> items) {
        if (items == null) {
            return;
        }
        checklistRepository.deleteByTicketId(ticketId);
        int order = 0;
        for (MaintenanceChecklistFormDto item : items) {
            if (!StringUtils.hasText(item.getItemText())) {
                continue;
            }
            MaintenanceChecklist checklist = MaintenanceChecklist.builder()
                    .ticketId(ticketId)
                    .itemText(item.getItemText().trim())
                    .done(Boolean.TRUE.equals(item.getDone()))
                    .sortOrder(item.getSortOrder() == null ? order++ : item.getSortOrder())
                    .build();
            checklistRepository.save(checklist);
        }
    }

    private void applyForm(MaintenanceTicket ticket, MaintenanceTicketFormDto request) {
        if (request.getAssetId() != null) {
            assetService.load(request.getAssetId());
            ticket.setAssetId(request.getAssetId());
        } else {
            ticket.setAssetId(null);
        }
        if (request.getCustomerId() != null) {
            customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new BusinessException("Customer not found"));
            ticket.setCustomerId(request.getCustomerId());
        } else {
            ticket.setCustomerId(null);
        }
        ticket.setTitle(request.getTitle().trim());
        ticket.setDescription(trimToNull(request.getDescription()));
        if (StringUtils.hasText(request.getPriority())) {
            ticket.setPriority(request.getPriority().trim().toUpperCase(Locale.ROOT));
        }
        if (StringUtils.hasText(request.getTicketType())) {
            ticket.setTicketType(request.getTicketType().trim().toUpperCase(Locale.ROOT));
        }
        if (request.getTechnicianId() != null) {
            technicianService.load(request.getTechnicianId());
            ticket.setTechnicianId(request.getTechnicianId());
            if ("OPEN".equals(ticket.getStatus())) {
                ticket.setStatus("ASSIGNED");
            }
        }
        ticket.setSlaHours(request.getSlaHours());
    }

    private void assertEditable(MaintenanceTicket ticket) {
        if (!EDITABLE_STATUSES.contains(ticket.getStatus())) {
            throw new BusinessException("Ticket cannot be edited in status " + ticket.getStatus());
        }
    }

    private MaintenanceSparePart loadSparePart(Long sparePartId, Long ticketId) {
        MaintenanceSparePart sparePart = sparePartRepository.findById(sparePartId)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceSparePart", sparePartId));
        if (!ticketId.equals(sparePart.getTicketId())) {
            throw new BusinessException("Spare part does not belong to this ticket");
        }
        return sparePart;
    }

    private MaintenanceTicket load(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceTicket", id));
    }

    private String resolveTicketNo(String requested) {
        String normalized = requested == null ? null : requested.trim();
        if (StringUtils.hasText(normalized)) {
            if (ticketRepository.existsByTicketNoIgnoreCase(normalized)) {
                throw new BusinessException("Ticket number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("MAINT_TICKET");
        } catch (Exception ex) {
            return "MTK-" + System.currentTimeMillis();
        }
    }

    private MaintenanceTicketDisplayDto toSummaryDisplay(MaintenanceTicket ticket) {
        return MaintenanceTicketDisplayDto.builder()
                .id(ticket.getId())
                .ticketNo(ticket.getTicketNo())
                .assetId(ticket.getAssetId())
                .assetCode(resolveAssetCode(ticket.getAssetId()))
                .assetName(resolveAssetName(ticket.getAssetId()))
                .customerId(ticket.getCustomerId())
                .customerName(resolveCustomerName(ticket.getCustomerId()))
                .title(ticket.getTitle())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .ticketType(ticket.getTicketType())
                .technicianId(ticket.getTechnicianId())
                .technicianName(resolveTechnicianName(ticket.getTechnicianId()))
                .slaHours(ticket.getSlaHours())
                .openedAt(ticket.getOpenedAt())
                .closedAt(ticket.getClosedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private MaintenanceTicketDisplayDto toDetailDisplay(MaintenanceTicket ticket) {
        MaintenanceTicketDisplayDto summary = toSummaryDisplay(ticket);
        summary.setDescription(ticket.getDescription());
        summary.setChecklists(checklistRepository.findByTicketIdOrderBySortOrderAscIdAsc(ticket.getId()).stream()
                .map(this::toChecklistDisplay).toList());
        summary.setSpareParts(sparePartRepository.findByTicketIdOrderByIdAsc(ticket.getId()).stream()
                .map(this::toSparePartDisplay).toList());
        summary.setCreatedBy(ticket.getCreatedBy());
        summary.setUpdatedBy(ticket.getUpdatedBy());
        return summary;
    }

    private MaintenanceChecklistDisplayDto toChecklistDisplay(MaintenanceChecklist item) {
        return MaintenanceChecklistDisplayDto.builder()
                .id(item.getId())
                .ticketId(item.getTicketId())
                .itemText(item.getItemText())
                .done(item.isDone())
                .sortOrder(item.getSortOrder())
                .build();
    }

    private MaintenanceSparePartDisplayDto toSparePartDisplay(MaintenanceSparePart sparePart) {
        Product product = productRepository.findById(sparePart.getProductId()).orElse(null);
        Warehouse warehouse = warehouseRepository.findById(sparePart.getWarehouseId()).orElse(null);
        return MaintenanceSparePartDisplayDto.builder()
                .id(sparePart.getId())
                .ticketId(sparePart.getTicketId())
                .productId(sparePart.getProductId())
                .productCode(product != null ? product.getCode() : null)
                .productName(product != null ? product.getNameEn() : null)
                .warehouseId(sparePart.getWarehouseId())
                .warehouseName(warehouse != null ? warehouse.getNameEn() : null)
                .quantity(sparePart.getQuantity())
                .unitCost(sparePart.getUnitCost())
                .movementId(sparePart.getMovementId())
                .issued(sparePart.getMovementId() != null)
                .build();
    }

    private String resolveAssetCode(Long assetId) {
        if (assetId == null) {
            return null;
        }
        try {
            return assetService.getById(assetId).getAssetCode();
        } catch (ResourceNotFoundException ex) {
            return null;
        }
    }

    private String resolveAssetName(Long assetId) {
        if (assetId == null) {
            return null;
        }
        try {
            return assetService.getById(assetId).getName();
        } catch (ResourceNotFoundException ex) {
            return null;
        }
    }

    private String resolveCustomerName(Long customerId) {
        if (customerId == null) {
            return null;
        }
        return customerRepository.findById(customerId).map(Customer::getNameEn).orElse(null);
    }

    private String resolveTechnicianName(Long technicianId) {
        if (technicianId == null) {
            return null;
        }
        try {
            return technicianService.getById(technicianId).getDisplayName();
        } catch (ResourceNotFoundException ex) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
