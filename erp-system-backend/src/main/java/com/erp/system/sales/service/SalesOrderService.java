package com.erp.system.sales.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.Warehouse;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.WarehouseRepository;
import com.erp.system.sales.domain.Customer;
import com.erp.system.sales.domain.SalesOrder;
import com.erp.system.sales.domain.SalesOrderLine;
import com.erp.system.sales.domain.SalesQuotation;
import com.erp.system.sales.dto.display.SalesOrderDisplayDto;
import com.erp.system.sales.dto.display.SalesOrderLineDisplayDto;
import com.erp.system.sales.dto.form.SalesInvoiceFormDto;
import com.erp.system.sales.dto.form.SalesInvoiceLineFormDto;
import com.erp.system.sales.dto.form.SalesOrderFormDto;
import com.erp.system.sales.dto.form.SalesOrderLineFormDto;
import com.erp.system.sales.repository.SalesOrderRepository;
import com.erp.system.sales.repository.SalesQuotationRepository;
import com.erp.system.sales.support.SalesDocumentTotalsSupport;
import com.erp.system.sales.support.SalesDocumentTotalsSupport.DocumentAmounts;
import com.erp.system.sales.support.SalesDocumentTotalsSupport.LineAmounts;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private static final String MODULE = "SALES";

    private final SalesOrderRepository orderRepository;
    private final SalesQuotationRepository quotationRepository;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<SalesOrderDisplayDto> getOrders(TransactionStatus status, String search,
                                                LocalDate fromDate, LocalDate toDate) {
        List<SalesOrder> orders = status == null
                ? orderRepository.findAllByOrderByOrderDateDescIdDesc()
                : orderRepository.findByStatusOrderByOrderDateDescIdDesc(status);

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return orders.stream()
                .filter(o -> fromDate == null || !o.getOrderDate().isBefore(fromDate))
                .filter(o -> toDate == null || !o.getOrderDate().isAfter(toDate))
                .filter(o -> normalizedSearch == null
                        || o.getOrderNumber().toLowerCase().contains(normalizedSearch)
                        || o.getCustomer().getCode().toLowerCase().contains(normalizedSearch)
                        || o.getCustomer().getNameEn().toLowerCase().contains(normalizedSearch))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesOrderDisplayDto getOrder(Long id) {
        return toDisplay(loadOrder(id));
    }

    @Transactional
    public SalesOrderDisplayDto createOrder(SalesOrderFormDto request) {
        SalesOrder order = SalesOrder.builder()
                .orderNumber(resolveOrderNumber(request.getOrderNumber()))
                .status(TransactionStatus.DRAFT)
                .lines(new ArrayList<>())
                .build();
        applyForm(order, request);
        order = orderRepository.save(order);

        activityLogService.log(MODULE, "CREATE", "SalesOrder", order.getId(), order.getOrderNumber(),
                "Created sales order " + order.getOrderNumber());
        return toDisplay(order);
    }

    @Transactional
    public SalesOrderDisplayDto updateOrder(Long id, SalesOrderFormDto request) {
        SalesOrder order = loadOrder(id);
        if (order.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft orders can be edited");
        }
        applyForm(order, request);
        order = orderRepository.save(order);

        activityLogService.log(MODULE, "UPDATE", "SalesOrder", order.getId(), order.getOrderNumber(),
                "Updated sales order " + order.getOrderNumber());
        return toDisplay(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        SalesOrder order = loadOrder(id);
        if (order.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft orders can be deleted");
        }
        orderRepository.delete(order);
        activityLogService.log(MODULE, "DELETE", "SalesOrder", order.getId(), order.getOrderNumber(),
                "Deleted sales order " + order.getOrderNumber());
    }

    @Transactional
    public SalesOrderDisplayDto approveOrder(Long id, String actor) {
        SalesOrder order = loadOrder(id);
        if (order.getStatus() == TransactionStatus.APPROVED) {
            return toDisplay(order);
        }
        if (order.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled orders cannot be approved");
        }
        if (order.getLines().isEmpty()) {
            throw new BusinessException("Order must have at least one line");
        }

        order.setStatus(TransactionStatus.APPROVED);
        order = orderRepository.save(order);

        activityLogService.log(MODULE, "APPROVE", "SalesOrder", order.getId(), order.getOrderNumber(),
                "Approved sales order " + order.getOrderNumber() + " by " + actor);
        return toDisplay(order);
    }

    @Transactional
    public SalesOrderDisplayDto cancelOrder(Long id, String actor, String reason) {
        SalesOrder order = loadOrder(id);
        if (order.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled");
        }
        order.setStatus(TransactionStatus.CANCELLED);
        order = orderRepository.save(order);

        String description = "Cancelled sales order " + order.getOrderNumber() + " by " + actor;
        if (reason != null && !reason.isBlank()) {
            description += ": " + reason.trim();
        }
        activityLogService.log(MODULE, "CANCEL", "SalesOrder", order.getId(), order.getOrderNumber(), description);
        return toDisplay(order);
    }

    @Transactional(readOnly = true)
    public SalesInvoiceFormDto buildInvoiceForm(Long id) {
        SalesOrder order = loadOrder(id);
        if (order.getStatus() != TransactionStatus.APPROVED) {
            throw new BusinessException("Only approved orders can be converted to invoices");
        }
        if (order.getLines() == null || order.getLines().isEmpty()) {
            throw new BusinessException("Order must have at least one line");
        }

        SalesInvoiceFormDto form = new SalesInvoiceFormDto();
        LocalDate invoiceDate = LocalDate.now();
        form.setInvoiceDate(invoiceDate);
        form.setDueDate(invoiceDate.plusDays(30));
        form.setCustomerId(order.getCustomer().getId());
        form.setOrderId(order.getId());
        Long warehouseId = order.getWarehouse() != null
                ? order.getWarehouse().getId()
                : resolveDefaultWarehouseId();
        form.setWarehouseId(warehouseId);
        form.setDiscountAmount(order.getDiscountAmount());
        form.setNotes(normalizeOptional(order.getNotes()));
        form.setLines(order.getLines().stream().map(line -> {
            SalesInvoiceLineFormDto mapped = new SalesInvoiceLineFormDto();
            mapped.setProductId(line.getProduct().getId());
            mapped.setDescription(line.getDescription());
            mapped.setQuantity(line.getQuantity());
            mapped.setUnitPrice(line.getUnitPrice());
            mapped.setDiscountPercent(line.getDiscountPercent());
            mapped.setTaxPercent(line.getTaxPercent());
            return mapped;
        }).toList());
        return form;
    }

    private Product loadProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private Warehouse loadWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    Long resolveDefaultWarehouseId() {
        List<Warehouse> active = warehouseRepository.findByActiveTrueOrderByCodeAsc();
        return active.stream()
                .filter(w -> "WH-MAIN".equalsIgnoreCase(w.getCode()))
                .findFirst()
                .or(() -> active.stream().findFirst())
                .map(Warehouse::getId)
                .orElseThrow(() -> new BusinessException("No active warehouse configured"));
    }

    SalesOrder loadOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", id));
    }

    private void applyForm(SalesOrder order, SalesOrderFormDto request) {
        Customer customer = customerService.loadCustomer(request.getCustomerId());
        if (!customer.isActive()) {
            throw new BusinessException("Customer must be active");
        }

        order.setOrderDate(request.getOrderDate());
        order.setCustomer(customer);
        order.setNotes(normalizeOptional(request.getNotes()));

        if (request.getQuotationId() != null) {
            SalesQuotation quotation = quotationRepository.findById(request.getQuotationId())
                    .orElseThrow(() -> new ResourceNotFoundException("SalesQuotation", request.getQuotationId()));
            order.setQuotation(quotation);
        } else {
            order.setQuotation(null);
        }

        if (request.getWarehouseId() != null) {
            Warehouse warehouse = loadWarehouse(request.getWarehouseId());
            if (!warehouse.isActive()) {
                throw new BusinessException("Warehouse must be active");
            }
            order.setWarehouse(warehouse);
        } else {
            order.setWarehouse(null);
        }

        order.getLines().clear();
        BigDecimal lineNetSubtotal = BigDecimal.ZERO;
        BigDecimal lineTaxTotal = BigDecimal.ZERO;

        for (SalesOrderLineFormDto lineRequest : request.getLines()) {
            Product product = loadProduct(lineRequest.getProductId());
            if (!product.isActive()) {
                throw new BusinessException("Product must be active: " + product.getCode());
            }

            LineAmounts amounts = SalesDocumentTotalsSupport.calculateLineAmounts(
                    lineRequest.getQuantity(),
                    lineRequest.getUnitPrice(),
                    lineRequest.getDiscountPercent(),
                    lineRequest.getTaxPercent());

            SalesOrderLine line = SalesOrderLine.builder()
                    .order(order)
                    .product(product)
                    .description(normalizeOptional(lineRequest.getDescription()))
                    .quantity(lineRequest.getQuantity())
                    .unitPrice(lineRequest.getUnitPrice())
                    .discountPercent(lineRequest.getDiscountPercent() != null ? lineRequest.getDiscountPercent() : BigDecimal.ZERO)
                    .taxPercent(lineRequest.getTaxPercent() != null ? lineRequest.getTaxPercent() : BigDecimal.ZERO)
                    .lineTotal(amounts.lineTotal())
                    .build();
            order.getLines().add(line);
            lineNetSubtotal = lineNetSubtotal.add(amounts.netAmount());
            lineTaxTotal = lineTaxTotal.add(amounts.taxAmount());
        }

        BigDecimal headerDiscount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        DocumentAmounts documentAmounts = SalesDocumentTotalsSupport.calculateDocumentAmounts(
                lineNetSubtotal, lineTaxTotal, headerDiscount);

        order.setSubtotal(documentAmounts.subtotal());
        order.setDiscountAmount(headerDiscount);
        order.setTaxAmount(documentAmounts.taxAmount());
        order.setTotalAmount(documentAmounts.totalAmount());
    }

    private String resolveOrderNumber(String orderNumber) {
        String normalized = normalizeOptional(orderNumber);
        if (normalized != null) {
            if (orderRepository.existsByOrderNumberIgnoreCase(normalized)) {
                throw new BusinessException("Order number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("SALES_ORDER");
        } catch (Exception exception) {
            return "SO-" + System.currentTimeMillis();
        }
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SalesOrderDisplayDto toDisplay(SalesOrder order) {
        Customer customer = order.getCustomer();
        SalesQuotation quotation = order.getQuotation();
        Warehouse warehouse = order.getWarehouse();
        return SalesOrderDisplayDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .customerId(customer.getId())
                .customerCode(customer.getCode())
                .customerName(resolveLocalizedName(customer.getNameEn(), customer.getNameAr()))
                .quotationId(quotation != null ? quotation.getId() : null)
                .quotationNumber(quotation != null ? quotation.getQuotationNumber() : null)
                .warehouseId(warehouse != null ? warehouse.getId() : null)
                .warehouseCode(warehouse != null ? warehouse.getCode() : null)
                .warehouseName(warehouse != null ? resolveLocalizedName(warehouse.getNameEn(), warehouse.getNameAr()) : null)
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .lines(order.getLines().stream().map(this::toLineDisplay).toList())
                .build();
    }

    private SalesOrderLineDisplayDto toLineDisplay(SalesOrderLine line) {
        Product product = line.getProduct();
        return SalesOrderLineDisplayDto.builder()
                .id(line.getId())
                .productId(product.getId())
                .productCode(product.getCode())
                .productName(resolveLocalizedName(product.getNameEn(), product.getNameAr()))
                .description(line.getDescription())
                .quantity(line.getQuantity())
                .unitPrice(line.getUnitPrice())
                .discountPercent(line.getDiscountPercent())
                .taxPercent(line.getTaxPercent())
                .lineTotal(line.getLineTotal())
                .build();
    }

    private String resolveLocalizedName(String nameEn, String nameAr) {
        if ("ar".equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage()) && nameAr != null) {
            return nameAr;
        }
        return nameEn;
    }
}
