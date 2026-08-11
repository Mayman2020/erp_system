package com.erp.system.purchases.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.Warehouse;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.WarehouseRepository;
import com.erp.system.inventory.service.StockService;
import com.erp.system.purchases.domain.GoodsReceipt;
import com.erp.system.purchases.domain.GoodsReceiptLine;
import com.erp.system.purchases.domain.Supplier;
import com.erp.system.purchases.dto.display.GoodsReceiptDisplayDto;
import com.erp.system.purchases.dto.display.GoodsReceiptLineDisplayDto;
import com.erp.system.purchases.dto.form.GoodsReceiptFormDto;
import com.erp.system.purchases.dto.form.GoodsReceiptLineInputDto;
import com.erp.system.purchases.repository.GoodsReceiptLineRepository;
import com.erp.system.purchases.repository.GoodsReceiptRepository;
import com.erp.system.purchases.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GoodsReceiptService {

    private static final String MODULE = "PURCHASES";

    private final GoodsReceiptRepository receiptRepository;
    private final GoodsReceiptLineRepository lineRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final StockService stockService;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<GoodsReceiptDisplayDto> getAll() {
        return receiptRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public GoodsReceiptDisplayDto getById(Long id) {
        return toDisplay(loadReceipt(id));
    }

    @Transactional
    public GoodsReceiptDisplayDto create(GoodsReceiptFormDto request) {
        GoodsReceipt receipt = GoodsReceipt.builder()
                .receiptNo(resolveReceiptNo(request.getReceiptNo()))
                .status("DRAFT")
                .build();
        applyForm(receipt, request);
        receipt = receiptRepository.save(receipt);
        replaceLines(receipt.getId(), request.getLines());
        activityLogService.log(MODULE, "CREATE", "GOODS_RECEIPT", receipt.getId(), receipt.getReceiptNo(),
                "Goods receipt created");
        return toDisplay(receipt);
    }

    @Transactional
    public GoodsReceiptDisplayDto update(Long id, GoodsReceiptFormDto request) {
        GoodsReceipt receipt = loadReceipt(id);
        if (!"DRAFT".equalsIgnoreCase(receipt.getStatus())) {
            throw new BusinessException("Only draft receipts can be edited");
        }
        applyForm(receipt, request);
        receipt = receiptRepository.save(receipt);
        replaceLines(receipt.getId(), request.getLines());
        activityLogService.log(MODULE, "UPDATE", "GOODS_RECEIPT", receipt.getId(), receipt.getReceiptNo(),
                "Goods receipt updated");
        return toDisplay(receipt);
    }

    @Transactional
    public GoodsReceiptDisplayDto approve(Long id) {
        GoodsReceipt receipt = loadReceipt(id);
        if ("APPROVED".equalsIgnoreCase(receipt.getStatus())) {
            return toDisplay(receipt);
        }
        if (!"DRAFT".equalsIgnoreCase(receipt.getStatus())) {
            throw new BusinessException("Only draft receipts can be approved");
        }
        List<GoodsReceiptLine> lines = lineRepository.findByReceiptIdOrderByIdAsc(receipt.getId());
        if (lines.isEmpty()) {
            throw new BusinessException("Receipt must have at least one line");
        }
        for (GoodsReceiptLine line : lines) {
            BigDecimal unitCost = line.getUnitCost() == null ? BigDecimal.ZERO : line.getUnitCost();
            stockService.receiveStock(
                    line.getProductId(),
                    receipt.getWarehouseId(),
                    line.getQuantity(),
                    unitCost,
                    "GOODS_RECEIPT",
                    receipt.getId(),
                    LocalDate.now());
        }
        receipt.setStatus("APPROVED");
        receipt.setReceivedAt(Instant.now());
        receipt = receiptRepository.save(receipt);
        activityLogService.log(MODULE, "APPROVE", "GOODS_RECEIPT", receipt.getId(), receipt.getReceiptNo(),
                "Goods receipt approved and stock updated");
        return toDisplay(receipt);
    }

    @Transactional
    public void delete(Long id) {
        GoodsReceipt receipt = loadReceipt(id);
        if (!"DRAFT".equalsIgnoreCase(receipt.getStatus())) {
            throw new BusinessException("Only draft receipts can be deleted");
        }
        lineRepository.findByReceiptIdOrderByIdAsc(id).forEach(lineRepository::delete);
        receiptRepository.delete(receipt);
        activityLogService.log(MODULE, "DELETE", "GOODS_RECEIPT", id, receipt.getReceiptNo(),
                "Goods receipt deleted");
    }

    private void applyForm(GoodsReceipt receipt, GoodsReceiptFormDto request) {
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BusinessException("Receipt must have at least one line");
        }
        if (request.getSupplierId() != null && !supplierRepository.existsById(request.getSupplierId())) {
            throw new ResourceNotFoundException("Supplier", request.getSupplierId());
        }
        warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehouseId()));
        receipt.setSupplierId(request.getSupplierId());
        receipt.setWarehouseId(request.getWarehouseId());
        receipt.setPurchaseOrderId(request.getPurchaseOrderId());
        receipt.setNotes(request.getNotes());
    }

    private void replaceLines(Long receiptId, List<GoodsReceiptLineInputDto> lineRequests) {
        lineRepository.findByReceiptIdOrderByIdAsc(receiptId).forEach(lineRepository::delete);
        for (GoodsReceiptLineInputDto lineRequest : lineRequests) {
            Product product = productRepository.findById(lineRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", lineRequest.getProductId()));
            BigDecimal unitCost = lineRequest.getUnitCost();
            if (unitCost == null || unitCost.compareTo(BigDecimal.ZERO) == 0) {
                unitCost = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
            }
            GoodsReceiptLine line = GoodsReceiptLine.builder()
                    .receiptId(receiptId)
                    .productId(lineRequest.getProductId())
                    .quantity(lineRequest.getQuantity())
                    .unitCost(unitCost)
                    .build();
            lineRepository.save(line);
        }
    }

    private String resolveReceiptNo(String requested) {
        if (StringUtils.hasText(requested)) {
            String normalized = requested.trim();
            if (receiptRepository.existsByReceiptNoIgnoreCase(normalized)) {
                throw new BusinessException("Receipt number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("GOODS_RECEIPT");
        } catch (Exception ex) {
            return "GR-" + System.currentTimeMillis();
        }
    }

    private GoodsReceipt loadReceipt(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoodsReceipt", id));
    }

    private GoodsReceiptDisplayDto toDisplay(GoodsReceipt receipt) {
        Warehouse warehouse = warehouseRepository.findById(receipt.getWarehouseId()).orElse(null);
        Supplier supplier = receipt.getSupplierId() == null ? null
                : supplierRepository.findById(receipt.getSupplierId()).orElse(null);
        List<GoodsReceiptLineDisplayDto> lines = lineRepository.findByReceiptIdOrderByIdAsc(receipt.getId()).stream()
                .map(this::toLineDisplay)
                .toList();
        return GoodsReceiptDisplayDto.builder()
                .id(receipt.getId())
                .receiptNo(receipt.getReceiptNo())
                .supplierId(receipt.getSupplierId())
                .supplierName(supplier == null ? null : supplier.getNameEn())
                .warehouseId(receipt.getWarehouseId())
                .warehouseCode(warehouse == null ? null : warehouse.getCode())
                .warehouseName(warehouse == null ? null : resolveWarehouseName(warehouse))
                .purchaseOrderId(receipt.getPurchaseOrderId())
                .status(receipt.getStatus())
                .receivedAt(receipt.getReceivedAt())
                .notes(receipt.getNotes())
                .lines(lines)
                .createdAt(receipt.getCreatedAt())
                .updatedAt(receipt.getUpdatedAt())
                .createdBy(receipt.getCreatedBy())
                .updatedBy(receipt.getUpdatedBy())
                .build();
    }

    private GoodsReceiptLineDisplayDto toLineDisplay(GoodsReceiptLine line) {
        Product product = productRepository.findById(line.getProductId()).orElse(null);
        return GoodsReceiptLineDisplayDto.builder()
                .id(line.getId())
                .receiptId(line.getReceiptId())
                .productId(line.getProductId())
                .productCode(product == null ? null : product.getCode())
                .productName(product == null ? null : resolveProductName(product))
                .quantity(line.getQuantity())
                .unitCost(line.getUnitCost())
                .build();
    }

    private String resolveProductName(Product product) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && "ar".equalsIgnoreCase(locale.getLanguage())
                && product.getNameAr() != null && !product.getNameAr().isBlank()) {
            return product.getNameAr();
        }
        return product.getNameEn();
    }

    private String resolveWarehouseName(Warehouse warehouse) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && "ar".equalsIgnoreCase(locale.getLanguage())
                && warehouse.getNameAr() != null && !warehouse.getNameAr().isBlank()) {
            return warehouse.getNameAr();
        }
        return warehouse.getNameEn();
    }
}
