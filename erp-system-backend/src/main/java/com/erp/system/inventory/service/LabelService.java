package com.erp.system.inventory.service;

import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.inventory.domain.Product;
import com.erp.system.inventory.domain.ProductBarcode;
import com.erp.system.inventory.dto.display.LabelPreviewDisplayDto;
import com.erp.system.inventory.repository.ProductBarcodeRepository;
import com.erp.system.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final ProductRepository productRepository;
    private final ProductBarcodeRepository barcodeRepository;

    @Transactional(readOnly = true)
    public LabelPreviewDisplayDto preview(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        String barcode = barcodeRepository.findFirstByProductIdAndPrimaryBarcodeTrue(productId)
                .map(ProductBarcode::getBarcode)
                .orElse(product.getBarcode() != null && !product.getBarcode().isBlank()
                        ? product.getBarcode()
                        : product.getCode());
        String qrPayload = product.getQrPayload();
        if (qrPayload == null || qrPayload.isBlank()) {
            qrPayload = "ERP|PID=" + product.getId() + "|CODE=" + product.getCode()
                    + "|PRICE=" + product.getSalePrice().toPlainString();
        }
        return LabelPreviewDisplayDto.builder()
                .productId(product.getId())
                .barcode(barcode)
                .qrPayload(qrPayload)
                .name(resolveProductName(product))
                .price(product.getSalePrice())
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
