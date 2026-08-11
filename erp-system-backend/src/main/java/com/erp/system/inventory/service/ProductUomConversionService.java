package com.erp.system.inventory.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.ProductUomConversion;
import com.erp.system.inventory.domain.UnitOfMeasure;
import com.erp.system.inventory.dto.display.ProductUomConversionDisplayDto;
import com.erp.system.inventory.dto.form.ProductUomConversionFormDto;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.ProductUomConversionRepository;
import com.erp.system.inventory.repository.UnitOfMeasureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProductUomConversionService {

    private final ProductRepository productRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ProductUomConversionRepository conversionRepository;

    @Transactional(readOnly = true)
    public List<ProductUomConversionDisplayDto> listByProduct(Long productId) {
        ensureProduct(productId);
        return conversionRepository.findByProductIdOrderByIdAsc(productId).stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductUomConversionDisplayDto getById(Long productId, Long id) {
        return toDisplay(loadConversion(productId, id));
    }

    @Transactional
    public ProductUomConversionDisplayDto create(Long productId, ProductUomConversionFormDto request) {
        Product product = ensureProduct(productId);
        if (conversionRepository.existsByProductIdAndUnitId(productId, request.getUnitId())) {
            throw new BusinessException("Unit conversion already exists for this product");
        }
        UnitOfMeasure unit = unitOfMeasureRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", request.getUnitId()));
        ProductUomConversion entity = ProductUomConversion.builder()
                .product(product)
                .unit(unit)
                .factorToBase(request.getFactorToBase())
                .purchase(Boolean.TRUE.equals(request.getPurchase()))
                .sales(Boolean.TRUE.equals(request.getSales()))
                .build();
        return toDisplay(conversionRepository.save(entity));
    }

    @Transactional
    public ProductUomConversionDisplayDto update(Long productId, Long id, ProductUomConversionFormDto request) {
        ProductUomConversion entity = loadConversion(productId, id);
        if (!entity.getUnit().getId().equals(request.getUnitId())
                && conversionRepository.existsByProductIdAndUnitId(productId, request.getUnitId())) {
            throw new BusinessException("Unit conversion already exists for this product");
        }
        UnitOfMeasure unit = unitOfMeasureRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", request.getUnitId()));
        entity.setUnit(unit);
        entity.setFactorToBase(request.getFactorToBase());
        entity.setPurchase(Boolean.TRUE.equals(request.getPurchase()));
        entity.setSales(Boolean.TRUE.equals(request.getSales()));
        return toDisplay(conversionRepository.save(entity));
    }

    @Transactional
    public void delete(Long productId, Long id) {
        conversionRepository.delete(loadConversion(productId, id));
    }

    private Product ensureProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    private ProductUomConversion loadConversion(Long productId, Long id) {
        return conversionRepository.findByIdAndProductId(id, productId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductUomConversion", id));
    }

    private ProductUomConversionDisplayDto toDisplay(ProductUomConversion entity) {
        UnitOfMeasure unit = entity.getUnit();
        return ProductUomConversionDisplayDto.builder()
                .id(entity.getId())
                .productId(entity.getProduct().getId())
                .unitId(unit.getId())
                .unitCode(unit.getCode())
                .unitName(resolveUnitName(unit))
                .factorToBase(entity.getFactorToBase())
                .purchase(entity.isPurchase())
                .sales(entity.isSales())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String resolveUnitName(UnitOfMeasure unit) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && "ar".equalsIgnoreCase(locale.getLanguage())
                && unit.getNameAr() != null && !unit.getNameAr().isBlank()) {
            return unit.getNameAr();
        }
        return unit.getNameEn();
    }
}
