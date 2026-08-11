package com.erp.system.purchases.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.purchases.domain.PurchaseRfq;
import com.erp.system.purchases.domain.PurchaseRfqLine;
import com.erp.system.purchases.domain.PurchaseRfqQuote;
import com.erp.system.purchases.domain.Supplier;
import com.erp.system.purchases.dto.display.PurchaseRfqDisplayDto;
import com.erp.system.purchases.dto.display.PurchaseRfqLineDisplayDto;
import com.erp.system.purchases.dto.display.PurchaseRfqQuoteDisplayDto;
import com.erp.system.purchases.dto.form.PurchaseRfqFormDto;
import com.erp.system.purchases.dto.form.PurchaseRfqLineInputDto;
import com.erp.system.purchases.dto.form.PurchaseRfqQuoteInputDto;
import com.erp.system.purchases.repository.PurchaseRfqLineRepository;
import com.erp.system.purchases.repository.PurchaseRfqQuoteRepository;
import com.erp.system.purchases.repository.PurchaseRfqRepository;
import com.erp.system.purchases.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PurchaseRfqService {

    private static final String MODULE = "PURCHASES";

    private final PurchaseRfqRepository rfqRepository;
    private final PurchaseRfqLineRepository lineRepository;
    private final PurchaseRfqQuoteRepository quoteRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<PurchaseRfqDisplayDto> getAll() {
        return rfqRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public PurchaseRfqDisplayDto getById(Long id) {
        return toDisplay(loadRfq(id));
    }

    @Transactional
    public PurchaseRfqDisplayDto create(PurchaseRfqFormDto request) {
        PurchaseRfq rfq = PurchaseRfq.builder()
                .rfqNo(resolveRfqNo(request.getRfqNo()))
                .status("DRAFT")
                .build();
        applyForm(rfq, request);
        rfq = rfqRepository.save(rfq);
        replaceLines(rfq.getId(), request.getLines());
        activityLogService.log(MODULE, "CREATE", "PURCHASE_RFQ", rfq.getId(), rfq.getRfqNo(), "RFQ created");
        return toDisplay(rfq);
    }

    @Transactional
    public PurchaseRfqDisplayDto update(Long id, PurchaseRfqFormDto request) {
        PurchaseRfq rfq = loadRfq(id);
        if (!"DRAFT".equalsIgnoreCase(rfq.getStatus())) {
            throw new BusinessException("Only draft RFQs can be edited");
        }
        applyForm(rfq, request);
        rfq = rfqRepository.save(rfq);
        replaceLines(rfq.getId(), request.getLines());
        activityLogService.log(MODULE, "UPDATE", "PURCHASE_RFQ", rfq.getId(), rfq.getRfqNo(), "RFQ updated");
        return toDisplay(rfq);
    }

    @Transactional
    public PurchaseRfqDisplayDto submit(Long id) {
        PurchaseRfq rfq = loadRfq(id);
        if (!"DRAFT".equalsIgnoreCase(rfq.getStatus())) {
            throw new BusinessException("Only draft RFQs can be submitted");
        }
        rfq.setStatus("OPEN");
        return toDisplay(rfqRepository.save(rfq));
    }

    @Transactional
    public PurchaseRfqQuoteDisplayDto addQuote(Long rfqId, PurchaseRfqQuoteInputDto request) {
        PurchaseRfq rfq = loadRfq(rfqId);
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));
        if (Boolean.TRUE.equals(request.getSelected())) {
            clearSelectedQuotes(rfqId);
        }
        PurchaseRfqQuote quote = PurchaseRfqQuote.builder()
                .rfqId(rfq.getId())
                .supplierId(supplier.getId())
                .unitPrice(request.getUnitPrice())
                .leadDays(request.getLeadDays() == null ? 0 : request.getLeadDays())
                .notes(request.getNotes())
                .selected(Boolean.TRUE.equals(request.getSelected()))
                .build();
        quote = quoteRepository.save(quote);
        return toQuoteDisplay(quote, supplier);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseRfq rfq = loadRfq(id);
        if (!"DRAFT".equalsIgnoreCase(rfq.getStatus())) {
            throw new BusinessException("Only draft RFQs can be deleted");
        }
        lineRepository.findByRfqIdOrderByIdAsc(id).forEach(lineRepository::delete);
        quoteRepository.findByRfqIdOrderByIdAsc(id).forEach(quoteRepository::delete);
        rfqRepository.delete(rfq);
        activityLogService.log(MODULE, "DELETE", "PURCHASE_RFQ", id, rfq.getRfqNo(), "RFQ deleted");
    }

    private void applyForm(PurchaseRfq rfq, PurchaseRfqFormDto request) {
        if (request.getLines() == null || request.getLines().isEmpty()) {
            throw new BusinessException("RFQ must have at least one line");
        }
        rfq.setTitle(request.getTitle().trim());
        rfq.setDueDate(request.getDueDate());
        rfq.setNotes(request.getNotes());
    }

    private void replaceLines(Long rfqId, List<PurchaseRfqLineInputDto> lineRequests) {
        lineRepository.findByRfqIdOrderByIdAsc(rfqId).forEach(lineRepository::delete);
        for (PurchaseRfqLineInputDto lineRequest : lineRequests) {
            if (!productRepository.existsById(lineRequest.getProductId())) {
                throw new BusinessException("Product not found: " + lineRequest.getProductId());
            }
            PurchaseRfqLine line = PurchaseRfqLine.builder()
                    .rfqId(rfqId)
                    .productId(lineRequest.getProductId())
                    .quantity(lineRequest.getQuantity())
                    .notes(lineRequest.getNotes())
                    .build();
            lineRepository.save(line);
        }
    }

    private void clearSelectedQuotes(Long rfqId) {
        quoteRepository.findByRfqIdOrderByIdAsc(rfqId).forEach(q -> {
            q.setSelected(false);
            quoteRepository.save(q);
        });
    }

    private String resolveRfqNo(String requested) {
        if (StringUtils.hasText(requested)) {
            String normalized = requested.trim();
            if (rfqRepository.existsByRfqNoIgnoreCase(normalized)) {
                throw new BusinessException("RFQ number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("PURCHASE_RFQ");
        } catch (Exception ex) {
            return "RFQ-" + System.currentTimeMillis();
        }
    }

    private PurchaseRfq loadRfq(Long id) {
        return rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRfq", id));
    }

    private PurchaseRfqDisplayDto toDisplay(PurchaseRfq rfq) {
        List<PurchaseRfqLineDisplayDto> lines = lineRepository.findByRfqIdOrderByIdAsc(rfq.getId()).stream()
                .map(this::toLineDisplay)
                .toList();
        List<PurchaseRfqQuoteDisplayDto> quotes = quoteRepository.findByRfqIdOrderByIdAsc(rfq.getId()).stream()
                .map(q -> {
                    Supplier supplier = supplierRepository.findById(q.getSupplierId()).orElse(null);
                    return toQuoteDisplay(q, supplier);
                })
                .toList();
        return PurchaseRfqDisplayDto.builder()
                .id(rfq.getId())
                .rfqNo(rfq.getRfqNo())
                .title(rfq.getTitle())
                .status(rfq.getStatus())
                .dueDate(rfq.getDueDate())
                .notes(rfq.getNotes())
                .lines(lines)
                .quotes(quotes)
                .createdAt(rfq.getCreatedAt())
                .updatedAt(rfq.getUpdatedAt())
                .createdBy(rfq.getCreatedBy())
                .updatedBy(rfq.getUpdatedBy())
                .build();
    }

    private PurchaseRfqLineDisplayDto toLineDisplay(PurchaseRfqLine line) {
        Product product = productRepository.findById(line.getProductId()).orElse(null);
        return PurchaseRfqLineDisplayDto.builder()
                .id(line.getId())
                .rfqId(line.getRfqId())
                .productId(line.getProductId())
                .productCode(product == null ? null : product.getCode())
                .productName(product == null ? null : resolveProductName(product))
                .quantity(line.getQuantity())
                .notes(line.getNotes())
                .build();
    }

    private PurchaseRfqQuoteDisplayDto toQuoteDisplay(PurchaseRfqQuote quote, Supplier supplier) {
        return PurchaseRfqQuoteDisplayDto.builder()
                .id(quote.getId())
                .rfqId(quote.getRfqId())
                .supplierId(quote.getSupplierId())
                .supplierName(supplier == null ? null : supplier.getNameEn())
                .unitPrice(quote.getUnitPrice())
                .leadDays(quote.getLeadDays())
                .notes(quote.getNotes())
                .selected(quote.isSelected())
                .createdAt(quote.getCreatedAt())
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
}
