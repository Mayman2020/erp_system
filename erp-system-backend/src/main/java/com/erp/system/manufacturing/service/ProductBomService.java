package com.erp.system.manufacturing.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.manufacturing.domain.ProductBomLine;
import com.erp.system.manufacturing.dto.display.ProductBomLineDisplayDto;
import com.erp.system.manufacturing.dto.form.ProductBomLineFormDto;
import com.erp.system.manufacturing.repository.ProductBomLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductBomService {

    private static final String MODULE = "MANUFACTURING";

    private final ProductBomLineRepository productBomLineRepository;
    private final ProductRepository productRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<ProductBomLineDisplayDto> getByParentProductId(Long parentProductId) {
        loadProduct(parentProductId);
        return productBomLineRepository.findByParentProductIdOrderByIdAsc(parentProductId).stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductBomLineDisplayDto getById(Long id) {
        return toDisplay(loadBomLine(id));
    }

    @Transactional
    public ProductBomLineDisplayDto create(ProductBomLineFormDto request) {
        ProductBomLine bomLine = new ProductBomLine();
        applyForm(bomLine, request);
        bomLine = productBomLineRepository.save(bomLine);
        activityLogService.log(
                MODULE,
                "CREATE",
                "ProductBomLine",
                bomLine.getId(),
                String.valueOf(bomLine.getParentProductId()),
                "Created BOM line " + bomLine.getId() + " for parent product " + bomLine.getParentProductId()
        );
        return toDisplay(bomLine);
    }

    @Transactional
    public ProductBomLineDisplayDto update(Long id, ProductBomLineFormDto request) {
        ProductBomLine bomLine = loadBomLine(id);
        applyForm(bomLine, request);
        bomLine = productBomLineRepository.save(bomLine);
        activityLogService.log(
                MODULE,
                "UPDATE",
                "ProductBomLine",
                bomLine.getId(),
                String.valueOf(bomLine.getParentProductId()),
                "Updated BOM line " + bomLine.getId() + " for parent product " + bomLine.getParentProductId()
        );
        return toDisplay(bomLine);
    }

    @Transactional
    public void delete(Long id) {
        ProductBomLine bomLine = loadBomLine(id);
        productBomLineRepository.delete(bomLine);
        activityLogService.log(
                MODULE,
                "DELETE",
                "ProductBomLine",
                id,
                String.valueOf(bomLine.getParentProductId()),
                "Deleted BOM line " + id + " for parent product " + bomLine.getParentProductId()
        );
    }

    private void applyForm(ProductBomLine bomLine, ProductBomLineFormDto request) {
        Product parent = loadProduct(request.getParentProductId());
        Product component = loadProduct(request.getComponentProductId());
        if (parent.getId().equals(component.getId())) {
            throw new BusinessException("Parent product and component product must be different");
        }

        boolean duplicate = productBomLineRepository.findByParentProductIdOrderByIdAsc(parent.getId()).stream()
                .anyMatch(existing -> existing.getComponentProductId().equals(component.getId())
                        && (bomLine.getId() == null || !existing.getId().equals(bomLine.getId())));
        if (duplicate) {
            throw new BusinessException("BOM line already exists for this parent and component product");
        }

        bomLine.setParentProductId(parent.getId());
        bomLine.setComponentProductId(component.getId());
        bomLine.setQuantityPerUnit(request.getQuantityPerUnit());
    }

    private ProductBomLine loadBomLine(Long id) {
        return productBomLineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBomLine", id));
    }

    private Product loadProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private ProductBomLineDisplayDto toDisplay(ProductBomLine bomLine) {
        Product parent = loadProduct(bomLine.getParentProductId());
        Product component = loadProduct(bomLine.getComponentProductId());
        return ProductBomLineDisplayDto.builder()
                .id(bomLine.getId())
                .parentProductId(parent.getId())
                .parentProductCode(parent.getCode())
                .parentProductName(parent.getNameEn())
                .componentProductId(component.getId())
                .componentProductCode(component.getCode())
                .componentProductName(component.getNameEn())
                .quantityPerUnit(bomLine.getQuantityPerUnit())
                .createdAt(bomLine.getCreatedAt())
                .updatedAt(bomLine.getUpdatedAt())
                .build();
    }
}
