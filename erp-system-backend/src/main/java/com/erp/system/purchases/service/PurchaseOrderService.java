package com.erp.system.purchases.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.purchases.domain.PurchaseOrder;
import com.erp.system.purchases.domain.PurchaseOrderLine;
import com.erp.system.purchases.dto.form.PurchaseInvoiceFormDto;
import com.erp.system.purchases.dto.form.PurchaseInvoiceLineInputDto;
import com.erp.system.purchases.dto.display.PurchaseOrderDisplayDto;
import com.erp.system.purchases.dto.display.PurchaseOrderLineDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseOrderFormDto;
import com.erp.system.purchases.dto.form.PurchaseOrderLineInputDto;
import com.erp.system.purchases.repository.PurchaseOrderLineRepository;
import com.erp.system.purchases.repository.PurchaseOrderRepository;
import com.erp.system.sales.support.SalesDocumentTotalsSupport;
import com.erp.system.sales.support.SalesDocumentTotalsSupport.DocumentAmounts;
import com.erp.system.sales.support.SalesDocumentTotalsSupport.LineAmounts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private static final String MODULE = "PURCHASES";

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final ProductRepository productRepository;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PurchaseOrderDisplayDto> getAll() {
        return purchaseOrderRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDisplayDto getById(Long id) {
        return toDisplay(loadPurchaseOrder(id));
    }

    @Transactional
    public PurchaseOrderDisplayDto create(PurchaseOrderFormDto request) {
        PurchaseOrder order = new PurchaseOrder();
        applyForm(order, request);
        order.setOrderNumber(resolveNumber(request.getOrderNumber()));
        order.setStatus(TransactionStatus.DRAFT);
        order = purchaseOrderRepository.save(order);
        replaceLines(order.getId(), request.getLines());
        order = purchaseOrderRepository.save(order);
        activityLogService.log(MODULE, "CREATE", "PurchaseOrder", order.getId(), order.getOrderNumber(),
                "Created purchase order " + order.getOrderNumber());
        return toDisplay(order);
    }

    @Transactional
    public PurchaseOrderDisplayDto update(Long id, PurchaseOrderFormDto request) {
        PurchaseOrder order = loadPurchaseOrder(id);
        if (order.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft orders can be edited");
        }
        applyForm(order, request);
        order = purchaseOrderRepository.save(order);
        replaceLines(order.getId(), request.getLines());
        order = purchaseOrderRepository.save(order);
        activityLogService.log(MODULE, "UPDATE", "PurchaseOrder", order.getId(), order.getOrderNumber(),
                "Updated purchase order " + order.getOrderNumber());
        return toDisplay(order);
    }

    @Transactional
    public PurchaseOrderDisplayDto approve(Long id, String actor) {
        PurchaseOrder order = loadPurchaseOrder(id);
        if (order.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled order cannot be approved");
        }
        order.setStatus(TransactionStatus.APPROVED);
        order.setUpdatedBy(actor);
        order = purchaseOrderRepository.save(order);
        activityLogService.log(MODULE, "APPROVE", "PurchaseOrder", order.getId(), order.getOrderNumber(),
                "Approved purchase order " + order.getOrderNumber());
        return toDisplay(order);
    }

    @Transactional
    public PurchaseOrderDisplayDto cancel(Long id, String actor, String reason) {
        PurchaseOrder order = loadPurchaseOrder(id);
        order.setStatus(TransactionStatus.CANCELLED);
        order.setUpdatedBy(actor);
        order = purchaseOrderRepository.save(order);
        activityLogService.log(MODULE, "CANCEL", "PurchaseOrder", order.getId(), order.getOrderNumber(),
                "Cancelled purchase order " + order.getOrderNumber());
        return toDisplay(order);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseOrder order = loadPurchaseOrder(id);
        if (order.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft orders can be deleted");
        }
        purchaseOrderRepository.delete(order);
        activityLogService.log(MODULE, "DELETE", "PurchaseOrder", id, order.getOrderNumber(),
                "Deleted purchase order " + order.getOrderNumber());
    }

    @Transactional(readOnly = true)
    public PurchaseInvoiceFormDto buildInvoiceForm(Long id) {
        PurchaseOrder order = loadPurchaseOrder(id);
        if (order.getStatus() != TransactionStatus.APPROVED) {
            throw new BusinessException("Only approved orders can be converted to invoices");
        }
        List<PurchaseOrderLine> orderLines = purchaseOrderLineRepository.findByOrderIdOrderByIdAsc(order.getId());
        if (orderLines.isEmpty()) {
            throw new BusinessException("Order must have at least one line");
        }

        PurchaseInvoiceFormDto form = new PurchaseInvoiceFormDto();
        LocalDate invoiceDate = LocalDate.now();
        form.setInvoiceDate(invoiceDate);
        form.setDueDate(invoiceDate.plusDays(30));
        form.setSupplierId(order.getSupplierId());
        form.setOrderId(order.getId());
        form.setWarehouseId(order.getWarehouseId());
        form.setDiscountAmount(order.getDiscountAmount());
        form.setNotes(order.getNotes());
        form.setLines(orderLines.stream().map(line -> {
            PurchaseInvoiceLineInputDto mapped = new PurchaseInvoiceLineInputDto();
            mapped.setProductId(line.getProductId());
            mapped.setDescription(line.getDescription());
            mapped.setQuantity(line.getQuantity());
            mapped.setUnitPrice(line.getUnitPrice());
            mapped.setDiscountPercent(line.getDiscountPercent());
            mapped.setTaxPercent(line.getTaxPercent());
            return mapped;
        }).toList());
        return form;
    }

    private String resolveNumber(String requested) {
        String normalized = requested == null ? null : requested.trim();
        if (normalized != null && !normalized.isEmpty()) {
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("PURCHASE_ORDER");
        } catch (Exception ex) {
            return "PO-" + System.currentTimeMillis();
        }
    }

    private void applyForm(PurchaseOrder order, PurchaseOrderFormDto request) {
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BusinessException("Order must have at least one line");
        }
        order.setOrderDate(request.getOrderDate());
        order.setSupplierId(request.getSupplierId());
        order.setWarehouseId(request.getWarehouseId());
        order.setNotes(request.getNotes());

        BigDecimal lineNetSubtotal = BigDecimal.ZERO;
        BigDecimal lineTaxTotal = BigDecimal.ZERO;
        for (PurchaseOrderLineInputDto lineRequest : request.getLines()) {
            LineAmounts amounts = SalesDocumentTotalsSupport.calculateLineAmounts(
                    lineRequest.getQuantity(),
                    lineRequest.getUnitPrice(),
                    lineRequest.getDiscountPercent(),
                    lineRequest.getTaxPercent());
            lineNetSubtotal = lineNetSubtotal.add(amounts.netAmount());
            lineTaxTotal = lineTaxTotal.add(amounts.taxAmount());
        }
        BigDecimal discount = request.getDiscountAmount() == null ? BigDecimal.ZERO : request.getDiscountAmount();
        DocumentAmounts totals = SalesDocumentTotalsSupport.calculateDocumentAmounts(lineNetSubtotal, lineTaxTotal, discount);
        order.setSubtotal(totals.subtotal());
        order.setDiscountAmount(discount);
        order.setTaxAmount(totals.taxAmount());
        order.setTotalAmount(totals.totalAmount());
    }

    private void replaceLines(Long orderId, List<PurchaseOrderLineInputDto> lineRequests) {
        List<PurchaseOrderLine> existing = purchaseOrderLineRepository.findByOrderIdOrderByIdAsc(orderId);
        if (!existing.isEmpty()) {
            purchaseOrderLineRepository.deleteAll(existing);
        }
        for (PurchaseOrderLineInputDto lineRequest : lineRequests) {
            if (!productRepository.existsById(lineRequest.getProductId())) {
                throw new BusinessException("Product not found: " + lineRequest.getProductId());
            }
            LineAmounts amounts = SalesDocumentTotalsSupport.calculateLineAmounts(
                    lineRequest.getQuantity(),
                    lineRequest.getUnitPrice(),
                    lineRequest.getDiscountPercent(),
                    lineRequest.getTaxPercent());
            PurchaseOrderLine line = PurchaseOrderLine.builder()
                    .orderId(orderId)
                    .productId(lineRequest.getProductId())
                    .description(lineRequest.getDescription())
                    .quantity(lineRequest.getQuantity())
                    .unitPrice(lineRequest.getUnitPrice())
                    .discountPercent(lineRequest.getDiscountPercent() != null ? lineRequest.getDiscountPercent() : BigDecimal.ZERO)
                    .taxPercent(lineRequest.getTaxPercent() != null ? lineRequest.getTaxPercent() : BigDecimal.ZERO)
                    .lineTotal(amounts.lineTotal())
                    .build();
            purchaseOrderLineRepository.save(line);
        }
    }

    private PurchaseOrder loadPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
    }

    private PurchaseOrderDisplayDto toDisplay(PurchaseOrder order) {
        List<PurchaseOrderLineDisplayDto> lines = purchaseOrderLineRepository.findByOrderIdOrderByIdAsc(order.getId())
                .stream()
                .map(line -> PurchaseOrderLineDisplayDto.builder()
                        .id(line.getId())
                        .orderId(line.getOrderId())
                        .productId(line.getProductId())
                        .description(line.getDescription())
                        .quantity(line.getQuantity())
                        .unitPrice(line.getUnitPrice())
                        .discountPercent(line.getDiscountPercent())
                        .taxPercent(line.getTaxPercent())
                        .lineTotal(line.getLineTotal())
                        .build())
                .toList();
        return PurchaseOrderDisplayDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .supplierId(order.getSupplierId())
                .warehouseId(order.getWarehouseId())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .lines(lines)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
