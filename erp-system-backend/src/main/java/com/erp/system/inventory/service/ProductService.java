package com.erp.system.inventory.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.ProductCategory;
import com.erp.system.inventory.domain.UnitOfMeasure;
import com.erp.system.inventory.dto.display.ProductDisplayDto;
import com.erp.system.inventory.dto.form.ProductFormDto;
import com.erp.system.inventory.repository.ProductCategoryRepository;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.StockLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String MODULE = "INVENTORY";

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final UnitOfMeasureService unitOfMeasureService;
    private final StockLevelRepository stockLevelRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<ProductDisplayDto> getProducts(Boolean active, Long categoryId, String search) {
        List<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.search(search.trim());
        } else if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findAllByOrderByCodeAsc();
        }

        return products.stream()
                .filter(product -> active == null || product.isActive() == active)
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDisplayDto getProduct(Long id) {
        return toDisplay(loadProduct(id));
    }

    @Transactional
    public ProductDisplayDto createProduct(ProductFormDto request) {
        String code = request.getCode().trim();
        if (productRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Product code already exists: " + code);
        }
        validateBarcode(request.getBarcode(), null);

        Product product = new Product();
        applyForm(product, request, code);
        product = productRepository.save(product);

        activityLogService.log(MODULE, "CREATE", "Product", product.getId(), product.getCode(),
                "Created product " + product.getCode());
        return toDisplay(product);
    }

    @Transactional
    public ProductDisplayDto updateProduct(Long id, ProductFormDto request) {
        Product product = loadProduct(id);
        String code = request.getCode().trim();
        if (!code.equalsIgnoreCase(product.getCode()) && productRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessException("Product code already exists: " + code);
        }
        validateBarcode(request.getBarcode(), id);

        applyForm(product, request, code);
        product = productRepository.save(product);

        activityLogService.log(MODULE, "UPDATE", "Product", product.getId(), product.getCode(),
                "Updated product " + product.getCode());
        return toDisplay(product);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = loadProduct(id);
        product.setActive(false);
        productRepository.save(product);
        activityLogService.log(MODULE, "DEACTIVATE", "Product", product.getId(), product.getCode(),
                "Deactivated product " + product.getCode());
    }

    @Transactional
    public void activateProduct(Long id) {
        Product product = loadProduct(id);
        product.setActive(true);
        productRepository.save(product);
        activityLogService.log(MODULE, "ACTIVATE", "Product", product.getId(), product.getCode(),
                "Activated product " + product.getCode());
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = loadProduct(id);
        if (!stockLevelRepository.findByProductIdOrderByWarehouse_CodeAsc(id).isEmpty()) {
            throw new BusinessException("Cannot delete product that has stock levels");
        }
        productRepository.delete(product);
        activityLogService.log(MODULE, "DELETE", "Product", id, product.getCode(),
                "Deleted product " + product.getCode());
    }

    private void applyForm(Product product, ProductFormDto request, String code) {
        UnitOfMeasure unit = unitOfMeasureService.loadUnit(request.getUnitId());
        if (!unit.isActive()) {
            throw new BusinessException("Unit of measure must be active");
        }

        ProductCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", request.getCategoryId()));
            if (!category.isActive()) {
                throw new BusinessException("Product category must be active");
            }
        }

        product.setCode(code);
        product.setBarcode(normalizeOptional(request.getBarcode()));
        product.setNameEn(request.getNameEn().trim());
        product.setNameAr(normalizeOptional(request.getNameAr()));
        product.setCategory(category);
        product.setUnit(unit);
        product.setCostPrice(request.getCostPrice() != null ? request.getCostPrice() : BigDecimal.ZERO);
        product.setSalePrice(request.getSalePrice() != null ? request.getSalePrice() : BigDecimal.ZERO);
        product.setReorderLevel(request.getReorderLevel() != null ? request.getReorderLevel() : BigDecimal.ZERO);
        product.setActive(request.getActive() == null || request.getActive());
        product.setDescription(normalizeOptional(request.getDescription()));
    }

    private void validateBarcode(String barcode, Long excludeId) {
        String normalized = normalizeOptional(barcode);
        if (normalized == null) {
            return;
        }
        boolean exists = excludeId == null
                ? productRepository.existsByBarcodeIgnoreCase(normalized)
                : productRepository.existsByBarcodeIgnoreCaseAndIdNot(normalized, excludeId);
        if (exists) {
            throw new BusinessException("Product barcode already exists: " + normalized);
        }
    }

    Product loadProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProductDisplayDto toDisplay(Product product) {
        BigDecimal totalQuantity = stockLevelRepository.sumQuantityByProductId(product.getId());
        return ProductDisplayDto.builder()
                .id(product.getId())
                .code(product.getCode())
                .barcode(product.getBarcode())
                .name(resolveLocalizedName(product.getNameEn(), product.getNameAr()))
                .nameEn(product.getNameEn())
                .nameAr(product.getNameAr())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryCode(product.getCategory() != null ? product.getCategory().getCode() : null)
                .categoryName(product.getCategory() != null
                        ? resolveLocalizedName(product.getCategory().getNameEn(), product.getCategory().getNameAr())
                        : null)
                .unitId(product.getUnit().getId())
                .unitCode(product.getUnit().getCode())
                .unitName(resolveLocalizedName(product.getUnit().getNameEn(), product.getUnit().getNameAr()))
                .costPrice(product.getCostPrice())
                .salePrice(product.getSalePrice())
                .reorderLevel(product.getReorderLevel())
                .active(product.isActive())
                .description(product.getDescription())
                .totalQuantity(totalQuantity)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private String resolveLocalizedName(String nameEn, String nameAr) {
        if ("ar".equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage()) && nameAr != null) {
            return nameAr;
        }
        return nameEn;
    }
}
