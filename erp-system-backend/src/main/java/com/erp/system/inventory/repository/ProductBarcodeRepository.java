package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.ProductBarcode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductBarcodeRepository extends JpaRepository<ProductBarcode, Long> {

    List<ProductBarcode> findByProductIdOrderByPrimaryBarcodeDescIdAsc(Long productId);

    Optional<ProductBarcode> findByIdAndProductId(Long id, Long productId);

    Optional<ProductBarcode> findFirstByProductIdAndPrimaryBarcodeTrue(Long productId);

    boolean existsByBarcodeIgnoreCase(String barcode);
}
