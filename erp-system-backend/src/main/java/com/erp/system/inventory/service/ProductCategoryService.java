package com.erp.system.inventory.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.erp.service.ActivityLogService;
import com.erp.system.inventory.domain.ProductCategory;
import com.erp.system.inventory.dto.display.ProductCategoryDisplayDto;
import com.erp.system.inventory.dto.form.ProductCategoryFormDto;
import com.erp.system.inventory.repository.ProductCategoryRepository;
import com.erp.system.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private static final String MODULE = "INVENTORY";

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ActivityLogService activityLogService;

    @Transactional(readOnly = true)
    public List<ProductCategoryDisplayDto> getCategories(Boolean active, String search) {
        List<ProductCategory> categories = search != null && !search.isBlank()
                ? categoryRepository.search(search.trim())
                : categoryRepository.findAllByOrderByCodeAsc();

        return categories.stream()
                .filter(category -> active == null || category.isActive() == active)
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductCategoryDisplayDto getCategory(Long id) {
        return toDisplay(loadCategory(id));
    }

    @Transactional
    public ProductCategoryDisplayDto createCategory(ProductCategoryFormDto request) {
        String code = request.getCode().trim();
        if (categoryRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Product category code already exists: " + code);
        }

        ProductCategory category = new ProductCategory();
        applyForm(category, request, code);
        category = categoryRepository.save(category);

        activityLogService.log(MODULE, "CREATE", "ProductCategory", category.getId(), category.getCode(),
                "Created product category " + category.getCode());
        return toDisplay(category);
    }

    @Transactional
    public ProductCategoryDisplayDto updateCategory(Long id, ProductCategoryFormDto request) {
        ProductCategory category = loadCategory(id);
        String code = request.getCode().trim();
        if (!code.equalsIgnoreCase(category.getCode()) && categoryRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BusinessException("Product category code already exists: " + code);
        }

        applyForm(category, request, code);
        category = categoryRepository.save(category);

        activityLogService.log(MODULE, "UPDATE", "ProductCategory", category.getId(), category.getCode(),
                "Updated product category " + category.getCode());
        return toDisplay(category);
    }

    @Transactional
    public void deactivateCategory(Long id) {
        ProductCategory category = loadCategory(id);
        category.setActive(false);
        categoryRepository.save(category);
        activityLogService.log(MODULE, "DEACTIVATE", "ProductCategory", category.getId(), category.getCode(),
                "Deactivated product category " + category.getCode());
    }

    @Transactional
    public void activateCategory(Long id) {
        ProductCategory category = loadCategory(id);
        category.setActive(true);
        categoryRepository.save(category);
        activityLogService.log(MODULE, "ACTIVATE", "ProductCategory", category.getId(), category.getCode(),
                "Activated product category " + category.getCode());
    }

    @Transactional
    public void deleteCategory(Long id) {
        ProductCategory category = loadCategory(id);
        if (productRepository.findByCategoryId(id).stream().findAny().isPresent()) {
            throw new BusinessException("Cannot delete category that has products");
        }
        categoryRepository.delete(category);
        activityLogService.log(MODULE, "DELETE", "ProductCategory", id, category.getCode(),
                "Deleted product category " + category.getCode());
    }

    private void applyForm(ProductCategory category, ProductCategoryFormDto request, String code) {
        ProductCategory parent = null;
        if (request.getParentId() != null) {
            parent = loadCategory(request.getParentId());
            if (!parent.isActive()) {
                throw new BusinessException("Parent category must be active");
            }
            if (category.getId() != null && isCircularReference(category.getId(), request.getParentId())) {
                throw new BusinessException("Circular hierarchy not allowed");
            }
        }

        category.setCode(code);
        category.setNameEn(request.getNameEn().trim());
        category.setNameAr(normalizeOptional(request.getNameAr()));
        category.setParent(parent);
        category.setActive(request.getActive() == null || request.getActive());
    }

    private boolean isCircularReference(Long categoryId, Long parentId) {
        Long currentId = parentId;
        while (currentId != null) {
            if (currentId.equals(categoryId)) {
                return true;
            }
            ProductCategory current = categoryRepository.findById(currentId).orElse(null);
            if (current == null || current.getParent() == null) {
                break;
            }
            currentId = current.getParent().getId();
        }
        return false;
    }

    private ProductCategory loadCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", id));
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProductCategoryDisplayDto toDisplay(ProductCategory category) {
        return ProductCategoryDisplayDto.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(resolveLocalizedName(category.getNameEn(), category.getNameAr()))
                .nameEn(category.getNameEn())
                .nameAr(category.getNameAr())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentCode(category.getParent() != null ? category.getParent().getCode() : null)
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private String resolveLocalizedName(String nameEn, String nameAr) {
        if ("ar".equalsIgnoreCase(LocaleContextHolder.getLocale().getLanguage()) && nameAr != null) {
            return nameAr;
        }
        return nameEn;
    }
}
