package com.erp.system.inventory.service;

import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.ProductBarcode;
import com.erp.system.inventory.dto.display.ProductBarcodeDisplayDto;
import com.erp.system.inventory.dto.form.ProductBarcodeFormDto;
import com.erp.system.inventory.repository.ProductBarcodeRepository;
import com.erp.system.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductBarcodeService {

    private final ProductRepository productRepository;
    private final ProductBarcodeRepository barcodeRepository;

    @Transactional(readOnly = true)
    public List<ProductBarcodeDisplayDto> listByProduct(Long productId) {
        ensureProduct(productId);
        return barcodeRepository.findByProductIdOrderByPrimaryBarcodeDescIdAsc(productId).stream()
                .map(this::toDisplay)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductBarcodeDisplayDto getById(Long productId, Long id) {
        return toDisplay(loadBarcode(productId, id));
    }

    @Transactional
    public ProductBarcodeDisplayDto create(Long productId, ProductBarcodeFormDto request) {
        Product product = ensureProduct(productId);
        String barcode = request.getBarcode().trim();
        if (barcodeRepository.existsByBarcodeIgnoreCase(barcode)) {
            throw new BusinessException("Barcode already exists");
        }
        boolean primary = Boolean.TRUE.equals(request.getPrimaryBarcode());
        if (primary) {
            clearPrimary(productId);
        }
        ProductBarcode entity = ProductBarcode.builder()
                .product(product)
                .barcode(barcode)
                .primaryBarcode(primary)
                .build();
        if (primary) {
            product.setBarcode(barcode);
            productRepository.save(product);
        }
        return toDisplay(barcodeRepository.save(entity));
    }

    @Transactional
    public ProductBarcodeDisplayDto update(Long productId, Long id, ProductBarcodeFormDto request) {
        ProductBarcode entity = loadBarcode(productId, id);
        String barcode = request.getBarcode().trim();
        if (!entity.getBarcode().equalsIgnoreCase(barcode) && barcodeRepository.existsByBarcodeIgnoreCase(barcode)) {
            throw new BusinessException("Barcode already exists");
        }
        boolean primary = Boolean.TRUE.equals(request.getPrimaryBarcode());
        if (primary) {
            clearPrimary(productId);
        }
        entity.setBarcode(barcode);
        entity.setPrimaryBarcode(primary);
        if (primary) {
            Product product = entity.getProduct();
            product.setBarcode(barcode);
            productRepository.save(product);
        }
        return toDisplay(barcodeRepository.save(entity));
    }

    @Transactional
    public void delete(Long productId, Long id) {
        barcodeRepository.delete(loadBarcode(productId, id));
    }

    private Product ensureProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    private ProductBarcode loadBarcode(Long productId, Long id) {
        return barcodeRepository.findByIdAndProductId(id, productId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductBarcode", id));
    }

    private void clearPrimary(Long productId) {
        barcodeRepository.findByProductIdOrderByPrimaryBarcodeDescIdAsc(productId).stream()
                .filter(ProductBarcode::isPrimaryBarcode)
                .forEach(row -> {
                    row.setPrimaryBarcode(false);
                    barcodeRepository.save(row);
                });
    }

    private ProductBarcodeDisplayDto toDisplay(ProductBarcode entity) {
        return ProductBarcodeDisplayDto.builder()
                .id(entity.getId())
                .productId(entity.getProduct().getId())
                .barcode(entity.getBarcode())
                .primaryBarcode(entity.isPrimaryBarcode())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
