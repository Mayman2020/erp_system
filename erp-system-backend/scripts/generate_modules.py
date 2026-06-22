#!/usr/bin/env python3
"""Generate purchases, hr, crm, projects (+ minimal inventory/sales) Java modules."""
import os
from pathlib import Path

BASE = Path(r"d:\Apps Work\My Apps\erp Project\erp-system-backend\src\main\java\com\erp\system")
files: dict[str, str] = {}

def w(path: str, content: str):
    files[path] = content

# --- inventory (minimal for purchases stock) ---
w("inventory/domain/Product.java", '''package com.erp.system.inventory.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products", schema = "erp_system")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;
    @Column(name = "name_en", nullable = false, length = 200)
    private String nameEn;
    @Column(name = "name_ar", length = 200)
    private String nameAr;
    @Column(name = "cost_price", nullable = false, precision = 19, scale = 2)
    @Builder.Default private BigDecimal costPrice = BigDecimal.ZERO;
    @Column(name = "sale_price", nullable = false, precision = 19, scale = 2)
    @Builder.Default private BigDecimal salePrice = BigDecimal.ZERO;
    @Column(name = "is_active", nullable = false)
    @Builder.Default private boolean active = true;
}
''')

w("inventory/domain/Warehouse.java", '''package com.erp.system.inventory.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouses", schema = "erp_system")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Warehouse extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code", nullable = false, length = 30, unique = true)
    private String code;
    @Column(name = "name_en", nullable = false, length = 150)
    private String nameEn;
    @Column(name = "name_ar", length = 150)
    private String nameAr;
    @Column(name = "is_active", nullable = false)
    @Builder.Default private boolean active = true;
}
''')

w("inventory/domain/StockLevel.java", '''package com.erp.system.inventory.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_levels", schema = "erp_system")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockLevel extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    @Builder.Default private BigDecimal quantity = BigDecimal.ZERO;
    @Column(name = "reserved_quantity", nullable = false, precision = 19, scale = 4)
    @Builder.Default private BigDecimal reservedQuantity = BigDecimal.ZERO;
}
''')

w("inventory/domain/StockMovement.java", '''package com.erp.system.inventory.domain;

import com.erp.system.common.entity.BaseEntity;
import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stock_movements", schema = "erp_system")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockMovement extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "movement_number", nullable = false, length = 50, unique = true)
    private String movementNumber;
    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private StockMovementType movementType;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 4)
    @Builder.Default private BigDecimal unitCost = BigDecimal.ZERO;
    @Column(name = "reference_type", length = 50)
    private String referenceType;
    @Column(name = "reference_id")
    private Long referenceId;
    @Column(name = "notes", length = 500)
    private String notes;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default private TransactionStatus status = TransactionStatus.APPROVED;
}
''')

w("inventory/repository/ProductRepository.java", '''package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrueOrderByCodeAsc();
    Optional<Product> findByCode(String code);
    boolean existsByCodeIgnoreCase(String code);
}
''')

w("inventory/repository/WarehouseRepository.java", '''package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    List<Warehouse> findByActiveTrueOrderByCodeAsc();
    Optional<Warehouse> findByCode(String code);
}
''')

w("inventory/repository/StockLevelRepository.java", '''package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {
    Optional<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
''')

w("inventory/repository/StockMovementRepository.java", '''package com.erp.system.inventory.repository;

import com.erp.system.inventory.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}
''')

w("inventory/service/StockService.java", '''package com.erp.system.inventory.service;

import com.erp.system.common.enums.StockMovementType;
import com.erp.system.common.enums.TransactionStatus;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.service.NumberingService;
import com.erp.system.inventory.domain.*;
import com.erp.system.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockMovementRepository stockMovementRepository;
    private final NumberingService numberingService;

    @Transactional
    public void receiveStock(Long productId, Long warehouseId, BigDecimal quantity, BigDecimal unitCost,
                             String referenceType, Long referenceId, LocalDate movementDate) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));
        if (!product.isActive() || !warehouse.isActive()) {
            throw new BusinessException("Product and warehouse must be active");
        }

        StockLevel level = stockLevelRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseGet(() -> StockLevel.builder().product(product).warehouse(warehouse).build());
        level.setQuantity(level.getQuantity().add(quantity));
        stockLevelRepository.save(level);

        String movementNumber;
        try {
            movementNumber = numberingService.generateNextNumber("STOCK_MOVEMENT");
        } catch (Exception e) {
            movementNumber = "SM-" + System.currentTimeMillis();
        }
        stockMovementRepository.save(StockMovement.builder()
                .movementNumber(movementNumber)
                .movementDate(movementDate)
                .movementType(StockMovementType.IN)
                .product(product)
                .warehouse(warehouse)
                .quantity(quantity)
                .unitCost(unitCost == null ? BigDecimal.ZERO : unitCost)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .status(TransactionStatus.APPROVED)
                .build());
    }

    @Transactional
    public void issueStock(Long productId, Long warehouseId, BigDecimal quantity, BigDecimal unitCost,
                           String referenceType, Long referenceId, LocalDate movementDate) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }
        StockLevel level = stockLevelRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new BusinessException("Insufficient stock for product " + productId));
        if (level.getQuantity().compareTo(quantity) < 0) {
            throw new BusinessException("Insufficient stock for product " + productId);
        }
        level.setQuantity(level.getQuantity().subtract(quantity));
        stockLevelRepository.save(level);

        Product product = level.getProduct();
        Warehouse warehouse = level.getWarehouse();
        String movementNumber;
        try {
            movementNumber = numberingService.generateNextNumber("STOCK_MOVEMENT");
        } catch (Exception e) {
            movementNumber = "SM-" + System.currentTimeMillis();
        }
        stockMovementRepository.save(StockMovement.builder()
                .movementNumber(movementNumber)
                .movementDate(movementDate)
                .movementType(StockMovementType.OUT)
                .product(product)
                .warehouse(warehouse)
                .quantity(quantity)
                .unitCost(unitCost == null ? BigDecimal.ZERO : unitCost)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .status(TransactionStatus.APPROVED)
                .build());
    }
}
''')

# --- sales Customer minimal ---
w("sales/domain/Customer.java", '''package com.erp.system.sales.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers", schema = "erp_system")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;
    @Column(name = "name_en", nullable = false, length = 200)
    private String nameEn;
    @Column(name = "name_ar", length = 200)
    private String nameAr;
    @Column(name = "email", length = 190)
    private String email;
    @Column(name = "phone", length = 30)
    private String phone;
    @Column(name = "is_active", nullable = false)
    @Builder.Default private boolean active = true;
}
''')

w("sales/repository/CustomerRepository.java", '''package com.erp.system.sales.repository;

import com.erp.system.sales.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByActiveTrueOrderByCodeAsc();
    Optional<Customer> findByCode(String code);
}
''')

print(f"Prepared {len(files)} files so far (part 1)")
