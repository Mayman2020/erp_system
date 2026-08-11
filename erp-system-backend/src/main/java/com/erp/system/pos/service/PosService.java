package com.erp.system.pos.service;

import com.erp.system.auth.domain.User;
import com.erp.system.auth.repository.UserRepository;
import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.security.JwtPrincipal;
import com.erp.system.common.service.NumberingService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.Warehouse;
import com.erp.system.inventory.dto.form.StockMovementFormDto;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.WarehouseRepository;
import com.erp.system.inventory.service.StockService;
import com.erp.system.pos.domain.*;
import com.erp.system.pos.dto.display.*;
import com.erp.system.pos.dto.form.*;
import com.erp.system.pos.repository.*;
import com.erp.system.sales.domain.Customer;
import com.erp.system.sales.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PosService {

    private final PosTerminalRepository terminalRepository;
    private final PosShiftRepository shiftRepository;
    private final PosSaleRepository saleRepository;
    private final PosOfflineBatchRepository offlineBatchRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final NumberingService numberingService;
    private final StockService stockService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<PosTerminalDisplayDto> listTerminals() {
        return terminalRepository.findByActiveTrueOrderByCodeAsc().stream().map(this::toTerminalDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PosShiftDisplayDto> listShifts() {
        return shiftRepository.findAllByOrderByOpenedAtDesc().stream().map(this::toShiftDto).toList();
    }

    @Transactional(readOnly = true)
    public PosShiftDisplayDto getOpenShiftForCurrentUser() {
        Long userId = currentUserId();
        return shiftRepository.findFirstByCashier_IdAndStatusOrderByOpenedAtDesc(userId, "OPEN")
                .map(this::toShiftDto)
                .orElse(null);
    }

    @Transactional
    public PosShiftDisplayDto openShift(PosOpenShiftFormDto request) {
        Long userId = currentUserId();
        shiftRepository.findFirstByCashier_IdAndStatusOrderByOpenedAtDesc(userId, "OPEN")
                .ifPresent(s -> { throw new BusinessException("POS.SHIFT_ALREADY_OPEN"); });

        PosTerminal terminal = terminalRepository.findById(request.getTerminalId())
                .orElseThrow(() -> new ResourceNotFoundException("POS terminal", request.getTerminalId()));
        Warehouse warehouse = request.getWarehouseId() != null
                ? warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", request.getWarehouseId()))
                : terminal.getWarehouse();
        User cashier = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        PosShift shift = PosShift.builder()
                .shiftNo(numberingService.generateNextNumber("POS_SHIFT"))
                .terminal(terminal)
                .cashier(cashier)
                .warehouse(warehouse)
                .status("OPEN")
                .openingCash(nz(request.getOpeningCash()))
                .cashSales(BigDecimal.ZERO)
                .cardSales(BigDecimal.ZERO)
                .creditSales(BigDecimal.ZERO)
                .notes(request.getNotes())
                .openedAt(Instant.now())
                .build();
        shift.setCreatedBy(cashier.getUsername());
        return toShiftDto(shiftRepository.save(shift));
    }

    @Transactional
    public PosShiftDisplayDto closeShift(Long shiftId, PosCloseShiftFormDto request) {
        PosShift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", shiftId));
        if (!"OPEN".equals(shift.getStatus())) {
            throw new BusinessException("POS.SHIFT_NOT_OPEN");
        }
        BigDecimal expected = nz(shift.getOpeningCash()).add(nz(shift.getCashSales()));
        BigDecimal closing = nz(request.getClosingCash());
        shift.setExpectedCash(expected);
        shift.setClosingCash(closing);
        shift.setDiscrepancy(closing.subtract(expected));
        shift.setNotes(request.getNotes());
        shift.setStatus("CLOSED");
        shift.setClosedAt(Instant.now());
        return toShiftDto(shiftRepository.save(shift));
    }

    @Transactional
    public PosSaleDisplayDto createSale(PosSaleFormDto request) {
        if (StringUtils.hasText(request.getIdempotencyKey())) {
            var existing = saleRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                return toSaleDto(existing.get());
            }
        }
        PosShift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift", request.getShiftId()));
        if (!"OPEN".equals(shift.getStatus())) {
            throw new BusinessException("POS.SHIFT_NOT_OPEN");
        }

        PosSale sale = PosSale.builder()
                .saleNo(numberingService.generateNextNumber("POS_SALE"))
                .shift(shift)
                .warehouse(shift.getWarehouse())
                .status("COMPLETED")
                .paymentMethod(StringUtils.hasText(request.getPaymentMethod()) ? request.getPaymentMethod() : "CASH")
                .discountAmount(nz(request.getDiscountAmount()))
                .paidCash(nz(request.getPaidCash()))
                .paidCard(nz(request.getPaidCard()))
                .paidCredit(nz(request.getPaidCredit()))
                .idempotencyKey(StringUtils.hasText(request.getIdempotencyKey()) ? request.getIdempotencyKey() : null)
                .offlineBatchId(request.getOfflineBatchId())
                .build();

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));
            sale.setCustomer(customer);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        for (PosSaleLineFormDto lineForm : request.getLines()) {
            Product product = productRepository.findById(lineForm.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", lineForm.getProductId()));
            BigDecimal qty = lineForm.getQuantity();
            BigDecimal price = lineForm.getUnitPrice();
            BigDecimal lineDisc = nz(lineForm.getDiscountAmount());
            BigDecimal lineNet = qty.multiply(price).subtract(lineDisc).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTax = lineNet.multiply(nz(lineForm.getTaxRate())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            PosSaleLine line = PosSaleLine.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(qty)
                    .unitPrice(price)
                    .discountAmount(lineDisc)
                    .taxRate(nz(lineForm.getTaxRate()))
                    .lineTotal(lineNet.add(lineTax))
                    .build();
            sale.getLines().add(line);
            subtotal = subtotal.add(lineNet);
            tax = tax.add(lineTax);

            StockMovementFormDto movement = new StockMovementFormDto();
            movement.setMovementDate(LocalDate.now());
            movement.setMovementType(StockMovementType.OUT);
            movement.setProductId(product.getId());
            movement.setWarehouseId(shift.getWarehouse().getId());
            movement.setQuantity(qty);
            movement.setUnitCost(product.getCostPrice());
            movement.setReferenceType("POS_SALE");
            movement.setNotes("POS " + sale.getSaleNo());
            movement.setApproveImmediately(true);
            stockService.stockOut(movement);
        }

        BigDecimal total = subtotal.subtract(nz(request.getDiscountAmount())).add(tax).setScale(2, RoundingMode.HALF_UP);
        sale.setSubtotal(subtotal);
        sale.setTaxAmount(tax);
        sale.setTotalAmount(total);

        if (sale.getPaidCash().add(sale.getPaidCard()).add(sale.getPaidCredit()).compareTo(BigDecimal.ZERO) == 0) {
            switch (sale.getPaymentMethod().toUpperCase()) {
                case "CARD" -> sale.setPaidCard(total);
                case "CREDIT" -> sale.setPaidCredit(total);
                default -> sale.setPaidCash(total);
            }
        }

        shift.setCashSales(nz(shift.getCashSales()).add(sale.getPaidCash()));
        shift.setCardSales(nz(shift.getCardSales()).add(sale.getPaidCard()));
        shift.setCreditSales(nz(shift.getCreditSales()).add(sale.getPaidCredit()));
        shiftRepository.save(shift);

        return toSaleDto(saleRepository.save(sale));
    }

    @Transactional
    public PosOfflineSyncResultDto syncOffline(PosOfflineSyncFormDto request) {
        var existingBatch = offlineBatchRepository.findByBatchKey(request.getBatchKey());
        if (existingBatch.isPresent() && "PROCESSED".equals(existingBatch.get().getStatus())) {
            return PosOfflineSyncResultDto.builder()
                    .batchKey(request.getBatchKey())
                    .status("ALREADY_PROCESSED")
                    .accepted(0)
                    .skipped(request.getSales().size())
                    .sales(List.of())
                    .build();
        }

        PosOfflineBatch batch = existingBatch.orElseGet(PosOfflineBatch::new);
        batch.setBatchKey(request.getBatchKey());
        batch.setStatus("PROCESSING");
        batch.setReceivedAt(Instant.now());
        try {
            batch.setPayloadJson(objectMapper.writeValueAsString(request));
        } catch (Exception ignored) {
            batch.setPayloadJson("{}");
        }
        if (request.getTerminalId() != null) {
            terminalRepository.findById(request.getTerminalId()).ifPresent(batch::setTerminal);
        }
        offlineBatchRepository.save(batch);

        List<PosSaleDisplayDto> accepted = new ArrayList<>();
        int skipped = 0;
        for (PosSaleFormDto saleForm : request.getSales()) {
            saleForm.setOfflineBatchId(request.getBatchKey());
            if (!StringUtils.hasText(saleForm.getIdempotencyKey())) {
                saleForm.setIdempotencyKey(request.getBatchKey() + "-" + accepted.size() + skipped);
            }
            try {
                accepted.add(createSale(saleForm));
            } catch (Exception ex) {
                skipped++;
            }
        }
        batch.setStatus("PROCESSED");
        batch.setProcessedAt(Instant.now());
        try {
            batch.setResultJson(objectMapper.writeValueAsString(accepted.stream().map(PosSaleDisplayDto::getSaleNo).toList()));
        } catch (Exception ignored) {
            batch.setResultJson("[]");
        }
        offlineBatchRepository.save(batch);

        return PosOfflineSyncResultDto.builder()
                .batchKey(request.getBatchKey())
                .status("PROCESSED")
                .accepted(accepted.size())
                .skipped(skipped)
                .sales(accepted)
                .build();
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtPrincipal principal) {
            return principal.userId();
        }
        throw new BusinessException("AUTH.REQUIRED");
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private PosTerminalDisplayDto toTerminalDto(PosTerminal t) {
        return PosTerminalDisplayDto.builder()
                .id(t.getId())
                .code(t.getCode())
                .name(t.getName())
                .warehouseId(t.getWarehouse().getId())
                .warehouseName(t.getWarehouse().getNameEn() != null ? t.getWarehouse().getNameEn() : t.getWarehouse().getCode())
                .active(t.isActive())
                .build();
    }

    private PosShiftDisplayDto toShiftDto(PosShift s) {
        return PosShiftDisplayDto.builder()
                .id(s.getId())
                .shiftNo(s.getShiftNo())
                .terminalId(s.getTerminal().getId())
                .terminalCode(s.getTerminal().getCode())
                .warehouseId(s.getWarehouse().getId())
                .warehouseName(s.getWarehouse().getNameEn() != null ? s.getWarehouse().getNameEn() : s.getWarehouse().getCode())
                .cashierUserId(s.getCashier().getId())
                .cashierName(s.getCashier().getUsername())
                .status(s.getStatus())
                .openingCash(s.getOpeningCash())
                .closingCash(s.getClosingCash())
                .expectedCash(s.getExpectedCash())
                .cashSales(s.getCashSales())
                .cardSales(s.getCardSales())
                .creditSales(s.getCreditSales())
                .discrepancy(s.getDiscrepancy())
                .notes(s.getNotes())
                .openedAt(s.getOpenedAt())
                .closedAt(s.getClosedAt())
                .build();
    }

    private PosSaleDisplayDto toSaleDto(PosSale sale) {
        List<PosSaleLineDisplayDto> lines = sale.getLines().stream().map(l -> PosSaleLineDisplayDto.builder()
                .id(l.getId())
                .productId(l.getProduct().getId())
                .productCode(l.getProduct().getCode())
                .productName(l.getProduct().getNameEn())
                .quantity(l.getQuantity())
                .unitPrice(l.getUnitPrice())
                .discountAmount(l.getDiscountAmount())
                .taxRate(l.getTaxRate())
                .lineTotal(l.getLineTotal())
                .build()).toList();
        return PosSaleDisplayDto.builder()
                .id(sale.getId())
                .saleNo(sale.getSaleNo())
                .shiftId(sale.getShift().getId())
                .warehouseId(sale.getWarehouse().getId())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getId() : null)
                .status(sale.getStatus())
                .subtotal(sale.getSubtotal())
                .discountAmount(sale.getDiscountAmount())
                .taxAmount(sale.getTaxAmount())
                .totalAmount(sale.getTotalAmount())
                .paymentMethod(sale.getPaymentMethod())
                .paidCash(sale.getPaidCash())
                .paidCard(sale.getPaidCard())
                .paidCredit(sale.getPaidCredit())
                .idempotencyKey(sale.getIdempotencyKey())
                .createdAt(sale.getCreatedAt())
                .lines(lines)
                .build();
    }
}
