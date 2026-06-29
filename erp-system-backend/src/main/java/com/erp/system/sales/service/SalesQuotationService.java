package com.erp.system.sales.service;

import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.sales.domain.Customer;
import com.erp.system.sales.domain.SalesQuotation;
import com.erp.system.sales.domain.SalesQuotationLine;
import com.erp.system.sales.dto.display.SalesOrderDisplayDto;
import com.erp.system.sales.dto.display.SalesQuotationDisplayDto;
import com.erp.system.sales.dto.display.SalesQuotationLineDisplayDto;
import com.erp.system.sales.dto.form.SalesOrderFormDto;
import com.erp.system.sales.dto.form.SalesOrderLineFormDto;
import com.erp.system.sales.dto.form.SalesQuotationFormDto;
import com.erp.system.sales.dto.form.SalesQuotationLineFormDto;
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
public class SalesQuotationService {

    private static final String MODULE = "SALES";

    private final SalesQuotationRepository quotationRepository;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final NumberingService numberingService;
    private final ActivityLogService activityLogService;
    private final SalesOrderService salesOrderService;

    @Transactional(readOnly = true)
    public List<SalesQuotationDisplayDto> getQuotations(TransactionStatus status, String search,
                                                        LocalDate fromDate, LocalDate toDate) {
        List<SalesQuotation> quotations = status == null
                ? quotationRepository.findAllByOrderByQuotationDateDescIdDesc()
                : quotationRepository.findByStatusOrderByQuotationDateDescIdDesc(status);

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim().toLowerCase();
        return quotations.stream()
                .filter(q -> fromDate == null || !q.getQuotationDate().isBefore(fromDate))
                .filter(q -> toDate == null || !q.getQuotationDate().isAfter(toDate))
                .filter(q -> normalizedSearch == null
                        || q.getQuotationNumber().toLowerCase().contains(normalizedSearch)
                        || q.getCustomer().getCode().toLowerCase().contains(normalizedSearch)
                        || q.getCustomer().getNameEn().toLowerCase().contains(normalizedSearch))
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesQuotationDisplayDto getQuotation(Long id) {
        return toDisplay(loadQuotation(id));
    }

    @Transactional
    public SalesQuotationDisplayDto createQuotation(SalesQuotationFormDto request) {
        SalesQuotation quotation = SalesQuotation.builder()
                .quotationNumber(resolveQuotationNumber(request.getQuotationNumber()))
                .status(TransactionStatus.DRAFT)
                .lines(new ArrayList<>())
                .build();
        applyForm(quotation, request);
        quotation = quotationRepository.save(quotation);

        activityLogService.log(MODULE, "CREATE", "SalesQuotation", quotation.getId(), quotation.getQuotationNumber(),
                "Created sales quotation " + quotation.getQuotationNumber());
        return toDisplay(quotation);
    }

    @Transactional
    public SalesQuotationDisplayDto updateQuotation(Long id, SalesQuotationFormDto request) {
        SalesQuotation quotation = loadQuotation(id);
        if (quotation.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft quotations can be edited");
        }
        applyForm(quotation, request);
        quotation = quotationRepository.save(quotation);

        activityLogService.log(MODULE, "UPDATE", "SalesQuotation", quotation.getId(), quotation.getQuotationNumber(),
                "Updated sales quotation " + quotation.getQuotationNumber());
        return toDisplay(quotation);
    }

    @Transactional
    public void deleteQuotation(Long id) {
        SalesQuotation quotation = loadQuotation(id);
        if (quotation.getStatus() != TransactionStatus.DRAFT) {
            throw new BusinessException("Only draft quotations can be deleted");
        }
        quotationRepository.delete(quotation);
        activityLogService.log(MODULE, "DELETE", "SalesQuotation", quotation.getId(), quotation.getQuotationNumber(),
                "Deleted sales quotation " + quotation.getQuotationNumber());
    }

    @Transactional
    public SalesQuotationDisplayDto approveQuotation(Long id, String actor) {
        SalesQuotation quotation = loadQuotation(id);
        if (quotation.getStatus() == TransactionStatus.APPROVED) {
            return toDisplay(quotation);
        }
        if (quotation.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Cancelled quotations cannot be approved");
        }
        if (quotation.getLines().isEmpty()) {
            throw new BusinessException("Quotation must have at least one line");
        }

        quotation.setStatus(TransactionStatus.APPROVED);
        quotation = quotationRepository.save(quotation);

        activityLogService.log(MODULE, "APPROVE", "SalesQuotation", quotation.getId(), quotation.getQuotationNumber(),
                "Approved sales quotation " + quotation.getQuotationNumber() + " by " + actor);
        return toDisplay(quotation);
    }

    @Transactional
    public SalesQuotationDisplayDto cancelQuotation(Long id, String actor, String reason) {
        SalesQuotation quotation = loadQuotation(id);
        if (quotation.getStatus() == TransactionStatus.CANCELLED) {
            throw new BusinessException("Quotation is already cancelled");
        }
        quotation.setStatus(TransactionStatus.CANCELLED);
        quotation = quotationRepository.save(quotation);

        String description = "Cancelled sales quotation " + quotation.getQuotationNumber() + " by " + actor;
        if (reason != null && !reason.isBlank()) {
            description += ": " + reason.trim();
        }
        activityLogService.log(MODULE, "CANCEL", "SalesQuotation", quotation.getId(), quotation.getQuotationNumber(), description);
        return toDisplay(quotation);
    }

    @Transactional
    public SalesOrderDisplayDto convertToOrder(Long id) {
        SalesQuotation quotation = loadQuotation(id);
        if (quotation.getStatus() != TransactionStatus.APPROVED) {
            throw new BusinessException("Only approved quotations can be converted to orders");
        }
        if (quotation.getLines() == null || quotation.getLines().isEmpty()) {
            throw new BusinessException("Quotation must have at least one line");
        }

        SalesOrderFormDto orderForm = new SalesOrderFormDto();
        orderForm.setOrderDate(LocalDate.now());
        orderForm.setCustomerId(quotation.getCustomer().getId());
        orderForm.setQuotationId(quotation.getId());
        orderForm.setWarehouseId(salesOrderService.resolveDefaultWarehouseId());
        orderForm.setDiscountAmount(quotation.getDiscountAmount());
        orderForm.setNotes(normalizeOptional(quotation.getNotes()));
        orderForm.setLines(quotation.getLines().stream().map(line -> {
            SalesOrderLineFormDto mapped = new SalesOrderLineFormDto();
            mapped.setProductId(line.getProduct().getId());
            mapped.setDescription(line.getDescription());
            mapped.setQuantity(line.getQuantity());
            mapped.setUnitPrice(line.getUnitPrice());
            mapped.setDiscountPercent(line.getDiscountPercent());
            mapped.setTaxPercent(line.getTaxPercent());
            return mapped;
        }).toList());
        return salesOrderService.createOrder(orderForm);
    }

    private void applyForm(SalesQuotation quotation, SalesQuotationFormDto request) {
        if (request.getValidUntil() != null && request.getValidUntil().isBefore(request.getQuotationDate())) {
            throw new BusinessException("Valid until date cannot be before quotation date");
        }

        Customer customer = customerService.loadCustomer(request.getCustomerId());
        if (!customer.isActive()) {
            throw new BusinessException("Customer must be active");
        }

        quotation.setQuotationDate(request.getQuotationDate());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setCustomer(customer);
        quotation.setNotes(normalizeOptional(request.getNotes()));

        quotation.getLines().clear();
        BigDecimal lineNetSubtotal = BigDecimal.ZERO;
        BigDecimal lineTaxTotal = BigDecimal.ZERO;

        for (SalesQuotationLineFormDto lineRequest : request.getLines()) {
            Product product = loadProduct(lineRequest.getProductId());
            if (!product.isActive()) {
                throw new BusinessException("Product must be active: " + product.getCode());
            }

            LineAmounts amounts = SalesDocumentTotalsSupport.calculateLineAmounts(
                    lineRequest.getQuantity(),
                    lineRequest.getUnitPrice(),
                    lineRequest.getDiscountPercent(),
                    lineRequest.getTaxPercent());

            SalesQuotationLine line = SalesQuotationLine.builder()
                    .quotation(quotation)
                    .product(product)
                    .description(normalizeOptional(lineRequest.getDescription()))
                    .quantity(lineRequest.getQuantity())
                    .unitPrice(lineRequest.getUnitPrice())
                    .discountPercent(lineRequest.getDiscountPercent() != null ? lineRequest.getDiscountPercent() : BigDecimal.ZERO)
                    .taxPercent(lineRequest.getTaxPercent() != null ? lineRequest.getTaxPercent() : BigDecimal.ZERO)
                    .lineTotal(amounts.lineTotal())
                    .build();
            quotation.getLines().add(line);
            lineNetSubtotal = lineNetSubtotal.add(amounts.netAmount());
            lineTaxTotal = lineTaxTotal.add(amounts.taxAmount());
        }

        BigDecimal headerDiscount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        DocumentAmounts documentAmounts = SalesDocumentTotalsSupport.calculateDocumentAmounts(
                lineNetSubtotal, lineTaxTotal, headerDiscount);

        quotation.setSubtotal(documentAmounts.subtotal());
        quotation.setDiscountAmount(headerDiscount);
        quotation.setTaxAmount(documentAmounts.taxAmount());
        quotation.setTotalAmount(documentAmounts.totalAmount());
    }

    private String resolveQuotationNumber(String quotationNumber) {
        String normalized = normalizeOptional(quotationNumber);
        if (normalized != null) {
            if (quotationRepository.existsByQuotationNumberIgnoreCase(normalized)) {
                throw new BusinessException("Quotation number already exists");
            }
            return normalized;
        }
        try {
            return numberingService.generateNextNumber("SALES_QUOTATION");
        } catch (Exception exception) {
            return "SQ-" + System.currentTimeMillis();
        }
    }

    private Product loadProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private SalesQuotation loadQuotation(Long id) {
        return quotationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesQuotation", id));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private SalesQuotationDisplayDto toDisplay(SalesQuotation quotation) {
        Customer customer = quotation.getCustomer();
        return SalesQuotationDisplayDto.builder()
                .id(quotation.getId())
                .quotationNumber(quotation.getQuotationNumber())
                .quotationDate(quotation.getQuotationDate())
                .validUntil(quotation.getValidUntil())
                .customerId(customer.getId())
                .customerCode(customer.getCode())
                .customerName(resolveLocalizedName(customer.getNameEn(), customer.getNameAr()))
                .status(quotation.getStatus())
                .subtotal(quotation.getSubtotal())
                .discountAmount(quotation.getDiscountAmount())
                .taxAmount(quotation.getTaxAmount())
                .totalAmount(quotation.getTotalAmount())
                .notes(quotation.getNotes())
                .createdAt(quotation.getCreatedAt())
                .updatedAt(quotation.getUpdatedAt())
                .lines(quotation.getLines().stream().map(this::toLineDisplay).toList())
                .build();
    }

    private SalesQuotationLineDisplayDto toLineDisplay(SalesQuotationLine line) {
        Product product = line.getProduct();
        return SalesQuotationLineDisplayDto.builder()
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
