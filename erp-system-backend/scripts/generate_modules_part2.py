#!/usr/bin/env python3
"""Generate purchases, hr, crm, and projects modules (part 2)."""
import os
from pathlib import Path
from textwrap import dedent

files: dict[str, str] = {}


def w(path: str, content: str) -> None:
    files[path] = dedent(content).strip() + "\n"


def lc(name: str) -> str:
    return name[:1].lower() + name[1:]


def add_standard_entity(
    module: str,
    module_code: str,
    entity: str,
    table: str,
    route: str,
    domain_fields: str,
    form_fields: str,
    display_fields: str,
    apply_form_lines: str,
    to_display_lines: str,
    repo_extra: str = "",
    service_extra: str = "",
    service_imports: str = "",
    controller_extra: str = "",
) -> None:
    var = lc(entity)
    repo = f"{entity}Repository"
    form = f"{entity}FormDto"
    display = f"{entity}DisplayDto"
    service = f"{entity}Service"
    controller = f"{entity}Controller"

    w(
        f"{module}/domain/{entity}.java",
        f"""
        package com.erp.system.{module}.domain;

        import com.erp.system.common.entity.BaseEntity;
        import jakarta.persistence.*;
        import lombok.*;

        import java.math.BigDecimal;
        import java.time.Instant;
        import java.time.LocalDate;
        import java.time.LocalDateTime;
        import java.time.LocalTime;

        @Entity
        @Table(name = "{table}", schema = "erp_system")
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public class {entity} extends BaseEntity {{

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;

        {domain_fields}
        }}
        """,
    )

    w(
        f"{module}/repository/{repo}.java",
        f"""
        package com.erp.system.{module}.repository;

        import com.erp.system.{module}.domain.{entity};
        import org.springframework.data.jpa.repository.JpaRepository;

        import java.util.List;

        public interface {repo} extends JpaRepository<{entity}, Long> {{
            List<{entity}> findAllByOrderByIdDesc();
        {repo_extra}
        }}
        """,
    )

    w(
        f"{module}/dto/form/{form}.java",
        f"""
        package com.erp.system.{module}.dto.form;

        import jakarta.validation.constraints.*;
        import lombok.*;

        import java.math.BigDecimal;
        import java.time.LocalDate;
        import java.time.LocalDateTime;
        import java.time.LocalTime;
        import java.util.List;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public class {form} {{
        {form_fields}
        }}
        """,
    )

    w(
        f"{module}/dto/display/{display}.java",
        f"""
        package com.erp.system.{module}.dto.display;

        import com.erp.system.common.enums.TransactionStatus;
        import lombok.*;

        import java.math.BigDecimal;
        import java.time.Instant;
        import java.time.LocalDate;
        import java.time.LocalDateTime;
        import java.time.LocalTime;
        import java.util.List;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public class {display} {{
            private Long id;
        {display_fields}
            private Instant createdAt;
            private Instant updatedAt;
        }}
        """,
    )

    w(
        f"{module}/service/{service}.java",
        f"""
        package com.erp.system.{module}.service;

        import com.erp.system.common.exception.ResourceNotFoundException;
        import com.erp.system.erp.service.ActivityLogService;
        import com.erp.system.{module}.domain.{entity};
        import com.erp.system.{module}.dto.display.{display};
        import com.erp.system.{module}.dto.form.{form};
        import com.erp.system.{module}.repository.{repo};
        import lombok.RequiredArgsConstructor;
        import org.springframework.stereotype.Service;
        import org.springframework.transaction.annotation.Transactional;

        import java.util.List;
        {service_imports}

        @Service
        @RequiredArgsConstructor
        public class {service} {{

            private static final String MODULE = "{module_code}";

            private final {repo} {var}Repository;
            private final ActivityLogService activityLogService;

            @Transactional(readOnly = true)
            public List<{display}> getAll() {{
                return {var}Repository.findAllByOrderByIdDesc().stream()
                        .map(this::toDisplay)
                        .toList();
            }}

            @Transactional(readOnly = true)
            public {display} getById(Long id) {{
                return toDisplay(load{entity}(id));
            }}

            @Transactional
            public {display} create({form} request) {{
                {entity} {var} = new {entity}();
                applyForm({var}, request);
                {var} = {var}Repository.save({var});
                activityLogService.log(MODULE, "CREATE", "{entity}", {var}.getId(), String.valueOf({var}.getId()),
                        "Created {entity} " + {var}.getId());
                return toDisplay({var});
            }}

            @Transactional
            public {display} update(Long id, {form} request) {{
                {entity} {var} = load{entity}(id);
                applyForm({var}, request);
                {var} = {var}Repository.save({var});
                activityLogService.log(MODULE, "UPDATE", "{entity}", {var}.getId(), String.valueOf({var}.getId()),
                        "Updated {entity} " + {var}.getId());
                return toDisplay({var});
            }}

            @Transactional
            public void delete(Long id) {{
                {entity} {var} = load{entity}(id);
                {var}Repository.delete({var});
                activityLogService.log(MODULE, "DELETE", "{entity}", id, String.valueOf(id),
                        "Deleted {entity} " + id);
            }}

            private {entity} load{entity}(Long id) {{
                return {var}Repository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("{entity}", id));
            }}

            private void applyForm({entity} {var}, {form} request) {{
        {apply_form_lines}
            }}

            private {display} toDisplay({entity} {var}) {{
                return {display}.builder()
                        .id({var}.getId())
        {to_display_lines}
                        .createdAt({var}.getCreatedAt())
                        .updatedAt({var}.getUpdatedAt())
                        .build();
            }}
        {service_extra}
        }}
        """,
    )

    w(
        f"{module}/controller/{controller}.java",
        f"""
        package com.erp.system.{module}.controller;

        import com.erp.system.common.dto.ApiResponse;
        import com.erp.system.{module}.dto.display.{display};
        import com.erp.system.{module}.dto.form.{form};
        import com.erp.system.{module}.service.{service};
        import jakarta.validation.Valid;
        import lombok.RequiredArgsConstructor;
        import org.springframework.http.HttpStatus;
        import org.springframework.web.bind.annotation.*;

        import java.util.List;

        @RestController
        @RequestMapping("/{module}/{route}")
        @RequiredArgsConstructor
        public class {controller} {{

            private final {service} {var}Service;

            @GetMapping
            public ApiResponse<List<{display}>> getAll() {{
                return ApiResponse.success({var}Service.getAll());
            }}

            @GetMapping("/{{id}}")
            public ApiResponse<{display}> getById(@PathVariable Long id) {{
                return ApiResponse.success({var}Service.getById(id));
            }}

            @PostMapping
            @ResponseStatus(HttpStatus.CREATED)
            public ApiResponse<{display}> create(@Valid @RequestBody {form} request) {{
                return ApiResponse.success({var}Service.create(request));
            }}

            @PutMapping("/{{id}}")
            public ApiResponse<{display}> update(@PathVariable Long id, @Valid @RequestBody {form} request) {{
                return ApiResponse.success({var}Service.update(id, request));
            }}

            @DeleteMapping("/{{id}}")
            public ApiResponse<Void> delete(@PathVariable Long id) {{
                {var}Service.delete(id);
                return ApiResponse.success(null);
            }}
        {controller_extra}
        }}
        """,
    )


# -----------------------------------------------------------------------------
# Shared enums required by request
# -----------------------------------------------------------------------------
w(
    "crm/domain/LeadStatus.java",
    """
    package com.erp.system.crm.domain;

    public enum LeadStatus {
        NEW,
        CONTACTED,
        QUALIFIED,
        PROPOSAL,
        WON,
        LOST
    }
    """,
)

w(
    "crm/domain/CrmActivityStatus.java",
    """
    package com.erp.system.crm.domain;

    public enum CrmActivityStatus {
        PLANNED,
        DONE,
        CANCELLED
    }
    """,
)

w(
    "projects/domain/ProjectStatus.java",
    """
    package com.erp.system.projects.domain;

    public enum ProjectStatus {
        PLANNING,
        IN_PROGRESS,
        ON_HOLD,
        COMPLETED,
        CANCELLED
    }
    """,
)

w(
    "projects/domain/TaskStatus.java",
    """
    package com.erp.system.projects.domain;

    public enum TaskStatus {
        TODO,
        IN_PROGRESS,
        DONE,
        BLOCKED
    }
    """,
)

w(
    "projects/domain/TaskPriority.java",
    """
    package com.erp.system.projects.domain;

    public enum TaskPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    """,
)

# -----------------------------------------------------------------------------
# Purchases
# -----------------------------------------------------------------------------
add_standard_entity(
    module="purchases",
    module_code="PURCHASES",
    entity="Supplier",
    table="suppliers",
    route="suppliers",
    domain_fields="""
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

        @Column(name = "tax_number", length = 50)
        private String taxNumber;

        @Column(name = "address", length = 500)
        private String address;

        @Column(name = "payable_account_id")
        private Long payableAccountId;

        @Column(name = "is_active", nullable = false)
        @Builder.Default
        private boolean active = true;
    """,
    form_fields="""
        @NotBlank
        @Size(max = 50)
        private String code;

        @NotBlank
        @Size(max = 200)
        private String nameEn;

        @Size(max = 200)
        private String nameAr;

        @Email
        @Size(max = 190)
        private String email;

        @Size(max = 30)
        private String phone;

        @Size(max = 50)
        private String taxNumber;

        @Size(max = 500)
        private String address;

        private Long payableAccountId;
        private Boolean active;
    """,
    display_fields="""
        private String code;
        private String nameEn;
        private String nameAr;
        private String email;
        private String phone;
        private String taxNumber;
        private String address;
        private Long payableAccountId;
        private boolean active;
    """,
    apply_form_lines="""
                supplier.setCode(request.getCode().trim());
                supplier.setNameEn(request.getNameEn().trim());
                supplier.setNameAr(request.getNameAr());
                supplier.setEmail(request.getEmail());
                supplier.setPhone(request.getPhone());
                supplier.setTaxNumber(request.getTaxNumber());
                supplier.setAddress(request.getAddress());
                supplier.setPayableAccountId(request.getPayableAccountId());
                supplier.setActive(request.getActive() == null || request.getActive());
    """,
    to_display_lines="""
                        .code(supplier.getCode())
                        .nameEn(supplier.getNameEn())
                        .nameAr(supplier.getNameAr())
                        .email(supplier.getEmail())
                        .phone(supplier.getPhone())
                        .taxNumber(supplier.getTaxNumber())
                        .address(supplier.getAddress())
                        .payableAccountId(supplier.getPayableAccountId())
                        .active(supplier.isActive())
    """,
    repo_extra="""
            boolean existsByCodeIgnoreCase(String code);
            boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    """,
)

add_standard_entity(
    module="purchases",
    module_code="PURCHASES",
    entity="PurchaseOrder",
    table="purchase_orders",
    route="orders",
    domain_fields="""
        @Column(name = "order_number", nullable = false, length = 50, unique = true)
        private String orderNumber;

        @Column(name = "order_date", nullable = false)
        private LocalDate orderDate;

        @Column(name = "supplier_id", nullable = false)
        private Long supplierId;

        @Column(name = "warehouse_id")
        private Long warehouseId;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private com.erp.system.common.enums.TransactionStatus status = com.erp.system.common.enums.TransactionStatus.DRAFT;

        @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal subtotal = BigDecimal.ZERO;

        @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal discountAmount = BigDecimal.ZERO;

        @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal taxAmount = BigDecimal.ZERO;

        @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal totalAmount = BigDecimal.ZERO;

        @Column(name = "notes", length = 500)
        private String notes;
    """,
    form_fields="""
        @Size(max = 50)
        private String orderNumber;

        @NotNull
        private LocalDate orderDate;

        @NotNull
        private Long supplierId;

        private Long warehouseId;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal subtotal;

        @DecimalMin("0.0")
        private BigDecimal discountAmount;

        @DecimalMin("0.0")
        private BigDecimal taxAmount;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal totalAmount;

        @Size(max = 500)
        private String notes;
    """,
    display_fields="""
        private String orderNumber;
        private LocalDate orderDate;
        private Long supplierId;
        private Long warehouseId;
        private TransactionStatus status;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private String notes;
    """,
    apply_form_lines="""
                purchaseOrder.setOrderNumber(request.getOrderNumber());
                purchaseOrder.setOrderDate(request.getOrderDate());
                purchaseOrder.setSupplierId(request.getSupplierId());
                purchaseOrder.setWarehouseId(request.getWarehouseId());
                purchaseOrder.setSubtotal(request.getSubtotal());
                purchaseOrder.setDiscountAmount(request.getDiscountAmount() == null ? BigDecimal.ZERO : request.getDiscountAmount());
                purchaseOrder.setTaxAmount(request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount());
                purchaseOrder.setTotalAmount(request.getTotalAmount());
                purchaseOrder.setNotes(request.getNotes());
    """,
    to_display_lines="""
                        .orderNumber(purchaseOrder.getOrderNumber())
                        .orderDate(purchaseOrder.getOrderDate())
                        .supplierId(purchaseOrder.getSupplierId())
                        .warehouseId(purchaseOrder.getWarehouseId())
                        .status(purchaseOrder.getStatus())
                        .subtotal(purchaseOrder.getSubtotal())
                        .discountAmount(purchaseOrder.getDiscountAmount())
                        .taxAmount(purchaseOrder.getTaxAmount())
                        .totalAmount(purchaseOrder.getTotalAmount())
                        .notes(purchaseOrder.getNotes())
    """,
    service_imports="import java.math.BigDecimal;",
)

add_standard_entity(
    module="purchases",
    module_code="PURCHASES",
    entity="PurchaseOrderLine",
    table="purchase_order_lines",
    route="order-lines",
    domain_fields="""
        @Column(name = "order_id", nullable = false)
        private Long orderId;

        @Column(name = "product_id", nullable = false)
        private Long productId;

        @Column(name = "description", length = 500)
        private String description;

        @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
        private BigDecimal quantity;

        @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
        private BigDecimal unitPrice;

        @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
        @Builder.Default
        private BigDecimal discountPercent = BigDecimal.ZERO;

        @Column(name = "tax_percent", nullable = false, precision = 5, scale = 2)
        @Builder.Default
        private BigDecimal taxPercent = BigDecimal.ZERO;

        @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal lineTotal = BigDecimal.ZERO;
    """,
    form_fields="""
        @NotNull
        private Long orderId;

        @NotNull
        private Long productId;

        @Size(max = 500)
        private String description;

        @NotNull
        @DecimalMin("0.0001")
        private BigDecimal quantity;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal unitPrice;

        @DecimalMin("0.0")
        private BigDecimal discountPercent;

        @DecimalMin("0.0")
        private BigDecimal taxPercent;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal lineTotal;
    """,
    display_fields="""
        private Long orderId;
        private Long productId;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountPercent;
        private BigDecimal taxPercent;
        private BigDecimal lineTotal;
    """,
    apply_form_lines="""
                purchaseOrderLine.setOrderId(request.getOrderId());
                purchaseOrderLine.setProductId(request.getProductId());
                purchaseOrderLine.setDescription(request.getDescription());
                purchaseOrderLine.setQuantity(request.getQuantity());
                purchaseOrderLine.setUnitPrice(request.getUnitPrice());
                purchaseOrderLine.setDiscountPercent(request.getDiscountPercent() == null ? BigDecimal.ZERO : request.getDiscountPercent());
                purchaseOrderLine.setTaxPercent(request.getTaxPercent() == null ? BigDecimal.ZERO : request.getTaxPercent());
                purchaseOrderLine.setLineTotal(request.getLineTotal());
    """,
    to_display_lines="""
                        .orderId(purchaseOrderLine.getOrderId())
                        .productId(purchaseOrderLine.getProductId())
                        .description(purchaseOrderLine.getDescription())
                        .quantity(purchaseOrderLine.getQuantity())
                        .unitPrice(purchaseOrderLine.getUnitPrice())
                        .discountPercent(purchaseOrderLine.getDiscountPercent())
                        .taxPercent(purchaseOrderLine.getTaxPercent())
                        .lineTotal(purchaseOrderLine.getLineTotal())
    """,
    repo_extra="""
            java.util.List<PurchaseOrderLine> findByOrderIdOrderByIdAsc(Long orderId);
    """,
    service_imports="import java.math.BigDecimal;",
)

add_standard_entity(
    module="purchases",
    module_code="PURCHASES",
    entity="PurchaseInvoiceLine",
    table="purchase_invoice_lines",
    route="invoice-lines",
    domain_fields="""
        @Column(name = "invoice_id", nullable = false)
        private Long invoiceId;

        @Column(name = "product_id", nullable = false)
        private Long productId;

        @Column(name = "description", length = 500)
        private String description;

        @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
        private BigDecimal quantity;

        @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
        private BigDecimal unitPrice;

        @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
        @Builder.Default
        private BigDecimal discountPercent = BigDecimal.ZERO;

        @Column(name = "tax_percent", nullable = false, precision = 5, scale = 2)
        @Builder.Default
        private BigDecimal taxPercent = BigDecimal.ZERO;

        @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal lineTotal = BigDecimal.ZERO;
    """,
    form_fields="""
        @NotNull
        private Long invoiceId;

        @NotNull
        private Long productId;

        @Size(max = 500)
        private String description;

        @NotNull
        @DecimalMin("0.0001")
        private BigDecimal quantity;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal unitPrice;

        @DecimalMin("0.0")
        private BigDecimal discountPercent;

        @DecimalMin("0.0")
        private BigDecimal taxPercent;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal lineTotal;
    """,
    display_fields="""
        private Long invoiceId;
        private Long productId;
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountPercent;
        private BigDecimal taxPercent;
        private BigDecimal lineTotal;
    """,
    apply_form_lines="""
                purchaseInvoiceLine.setInvoiceId(request.getInvoiceId());
                purchaseInvoiceLine.setProductId(request.getProductId());
                purchaseInvoiceLine.setDescription(request.getDescription());
                purchaseInvoiceLine.setQuantity(request.getQuantity());
                purchaseInvoiceLine.setUnitPrice(request.getUnitPrice());
                purchaseInvoiceLine.setDiscountPercent(request.getDiscountPercent() == null ? BigDecimal.ZERO : request.getDiscountPercent());
                purchaseInvoiceLine.setTaxPercent(request.getTaxPercent() == null ? BigDecimal.ZERO : request.getTaxPercent());
                purchaseInvoiceLine.setLineTotal(request.getLineTotal());
    """,
    to_display_lines="""
                        .invoiceId(purchaseInvoiceLine.getInvoiceId())
                        .productId(purchaseInvoiceLine.getProductId())
                        .description(purchaseInvoiceLine.getDescription())
                        .quantity(purchaseInvoiceLine.getQuantity())
                        .unitPrice(purchaseInvoiceLine.getUnitPrice())
                        .discountPercent(purchaseInvoiceLine.getDiscountPercent())
                        .taxPercent(purchaseInvoiceLine.getTaxPercent())
                        .lineTotal(purchaseInvoiceLine.getLineTotal())
    """,
    repo_extra="""
            java.util.List<PurchaseInvoiceLine> findByInvoiceIdOrderByIdAsc(Long invoiceId);
    """,
    service_imports="import java.math.BigDecimal;",
)

add_standard_entity(
    module="purchases",
    module_code="PURCHASES",
    entity="PurchaseReturn",
    table="purchase_returns",
    route="returns",
    domain_fields="""
        @Column(name = "return_number", nullable = false, length = 50, unique = true)
        private String returnNumber;

        @Column(name = "return_date", nullable = false)
        private LocalDate returnDate;

        @Column(name = "supplier_id", nullable = false)
        private Long supplierId;

        @Column(name = "invoice_id")
        private Long invoiceId;

        @Column(name = "warehouse_id")
        private Long warehouseId;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private com.erp.system.common.enums.TransactionStatus status = com.erp.system.common.enums.TransactionStatus.DRAFT;

        @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal subtotal = BigDecimal.ZERO;

        @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal taxAmount = BigDecimal.ZERO;

        @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal totalAmount = BigDecimal.ZERO;

        @Column(name = "notes", length = 500)
        private String notes;

        @Column(name = "journal_entry_id")
        private Long journalEntryId;
    """,
    form_fields="""
        @Size(max = 50)
        private String returnNumber;

        @NotNull
        private LocalDate returnDate;

        @NotNull
        private Long supplierId;

        private Long invoiceId;
        private Long warehouseId;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal subtotal;

        @DecimalMin("0.0")
        private BigDecimal taxAmount;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal totalAmount;

        @Size(max = 500)
        private String notes;
    """,
    display_fields="""
        private String returnNumber;
        private LocalDate returnDate;
        private Long supplierId;
        private Long invoiceId;
        private Long warehouseId;
        private TransactionStatus status;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private String notes;
        private Long journalEntryId;
    """,
    apply_form_lines="""
                purchaseReturn.setReturnNumber(request.getReturnNumber());
                purchaseReturn.setReturnDate(request.getReturnDate());
                purchaseReturn.setSupplierId(request.getSupplierId());
                purchaseReturn.setInvoiceId(request.getInvoiceId());
                purchaseReturn.setWarehouseId(request.getWarehouseId());
                purchaseReturn.setSubtotal(request.getSubtotal());
                purchaseReturn.setTaxAmount(request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount());
                purchaseReturn.setTotalAmount(request.getTotalAmount());
                purchaseReturn.setNotes(request.getNotes());
    """,
    to_display_lines="""
                        .returnNumber(purchaseReturn.getReturnNumber())
                        .returnDate(purchaseReturn.getReturnDate())
                        .supplierId(purchaseReturn.getSupplierId())
                        .invoiceId(purchaseReturn.getInvoiceId())
                        .warehouseId(purchaseReturn.getWarehouseId())
                        .status(purchaseReturn.getStatus())
                        .subtotal(purchaseReturn.getSubtotal())
                        .taxAmount(purchaseReturn.getTaxAmount())
                        .totalAmount(purchaseReturn.getTotalAmount())
                        .notes(purchaseReturn.getNotes())
                        .journalEntryId(purchaseReturn.getJournalEntryId())
    """,
    service_imports="import java.math.BigDecimal;",
)

add_standard_entity(
    module="purchases",
    module_code="PURCHASES",
    entity="PurchaseReturnLine",
    table="purchase_return_lines",
    route="return-lines",
    domain_fields="""
        @Column(name = "return_id", nullable = false)
        private Long returnId;

        @Column(name = "product_id", nullable = false)
        private Long productId;

        @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
        private BigDecimal quantity;

        @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
        private BigDecimal unitPrice;

        @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
        private BigDecimal lineTotal;
    """,
    form_fields="""
        @NotNull
        private Long returnId;

        @NotNull
        private Long productId;

        @NotNull
        @DecimalMin("0.0001")
        private BigDecimal quantity;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal unitPrice;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal lineTotal;
    """,
    display_fields="""
        private Long returnId;
        private Long productId;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    """,
    apply_form_lines="""
                purchaseReturnLine.setReturnId(request.getReturnId());
                purchaseReturnLine.setProductId(request.getProductId());
                purchaseReturnLine.setQuantity(request.getQuantity());
                purchaseReturnLine.setUnitPrice(request.getUnitPrice());
                purchaseReturnLine.setLineTotal(request.getLineTotal());
    """,
    to_display_lines="""
                        .returnId(purchaseReturnLine.getReturnId())
                        .productId(purchaseReturnLine.getProductId())
                        .quantity(purchaseReturnLine.getQuantity())
                        .unitPrice(purchaseReturnLine.getUnitPrice())
                        .lineTotal(purchaseReturnLine.getLineTotal())
    """,
    repo_extra="""
            java.util.List<PurchaseReturnLine> findByReturnIdOrderByIdAsc(Long returnId);
    """,
)

# Custom purchase invoice and supplier payment with posting logic
w(
    "purchases/domain/PurchaseInvoice.java",
    """
    package com.erp.system.purchases.domain;

    import com.erp.system.common.entity.BaseEntity;
    import com.erp.system.common.enums.TransactionStatus;
    import jakarta.persistence.*;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.time.LocalDateTime;

    @Entity
    @Table(name = "purchase_invoices", schema = "erp_system")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PurchaseInvoice extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "invoice_number", nullable = false, length = 50, unique = true)
        private String invoiceNumber;

        @Column(name = "invoice_date", nullable = false)
        private LocalDate invoiceDate;

        @Column(name = "due_date", nullable = false)
        private LocalDate dueDate;

        @Column(name = "supplier_id", nullable = false)
        private Long supplierId;

        @Column(name = "order_id")
        private Long orderId;

        @Column(name = "warehouse_id")
        private Long warehouseId;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private TransactionStatus status = TransactionStatus.DRAFT;

        @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal subtotal = BigDecimal.ZERO;

        @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal discountAmount = BigDecimal.ZERO;

        @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal taxAmount = BigDecimal.ZERO;

        @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal totalAmount = BigDecimal.ZERO;

        @Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal paidAmount = BigDecimal.ZERO;

        @Column(name = "remaining_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal remainingAmount = BigDecimal.ZERO;

        @Column(name = "notes", length = 500)
        private String notes;

        @Column(name = "journal_entry_id")
        private Long journalEntryId;

        @Column(name = "cancellation_journal_entry_id")
        private Long cancellationJournalEntryId;

        @Column(name = "approved_at")
        private LocalDateTime approvedAt;

        @Column(name = "approved_by", length = 100)
        private String approvedBy;

        @Column(name = "cancelled_at")
        private LocalDateTime cancelledAt;

        @Column(name = "cancelled_by", length = 100)
        private String cancelledBy;
    }
    """,
)

w(
    "purchases/repository/PurchaseInvoiceRepository.java",
    """
    package com.erp.system.purchases.repository;

    import com.erp.system.common.enums.TransactionStatus;
    import com.erp.system.purchases.domain.PurchaseInvoice;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;

    public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {
        List<PurchaseInvoice> findAllByOrderByIdDesc();
        List<PurchaseInvoice> findByStatusOrderByIdDesc(TransactionStatus status);
        boolean existsByInvoiceNumberIgnoreCase(String invoiceNumber);
        boolean existsByInvoiceNumberIgnoreCaseAndIdNot(String invoiceNumber, Long id);
    }
    """,
)

w(
    "purchases/dto/form/PurchaseInvoiceFormDto.java",
    """
    package com.erp.system.purchases.dto.form;

    import jakarta.validation.constraints.DecimalMin;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.LocalDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PurchaseInvoiceFormDto {
        @Size(max = 50)
        private String invoiceNumber;

        @NotNull
        private LocalDate invoiceDate;

        @NotNull
        private LocalDate dueDate;

        @NotNull
        private Long supplierId;

        private Long orderId;
        private Long warehouseId;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal subtotal;

        @DecimalMin("0.0")
        private BigDecimal discountAmount;

        @DecimalMin("0.0")
        private BigDecimal taxAmount;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal totalAmount;

        @Size(max = 500)
        private String notes;
    }
    """,
)

w(
    "purchases/dto/display/PurchaseInvoiceDisplayDto.java",
    """
    package com.erp.system.purchases.dto.display;

    import com.erp.system.common.enums.TransactionStatus;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.Instant;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.List;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PurchaseInvoiceDisplayDto {
        private Long id;
        private String invoiceNumber;
        private LocalDate invoiceDate;
        private LocalDate dueDate;
        private Long supplierId;
        private Long orderId;
        private Long warehouseId;
        private TransactionStatus status;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private BigDecimal remainingAmount;
        private String notes;
        private Long journalEntryId;
        private Long cancellationJournalEntryId;
        private LocalDateTime approvedAt;
        private String approvedBy;
        private LocalDateTime cancelledAt;
        private String cancelledBy;
        private List<PurchaseInvoiceLineDisplayDto> lines;
        private Instant createdAt;
        private Instant updatedAt;
    }
    """,
)

w(
    "purchases/service/PurchaseInvoiceService.java",
    """
    package com.erp.system.purchases.service;

    import com.erp.system.accounting.domain.Account;
    import com.erp.system.accounting.domain.JournalEntry;
    import com.erp.system.accounting.repository.AccountRepository;
    import com.erp.system.accounting.repository.JournalEntryRepository;
    import com.erp.system.accounting.service.AccountingPostingService;
    import com.erp.system.accounting.support.JournalPostingNarratives;
    import com.erp.system.common.enums.TransactionStatus;
    import com.erp.system.common.exception.BusinessException;
    import com.erp.system.common.exception.ResourceNotFoundException;
    import com.erp.system.common.service.NumberingService;
    import com.erp.system.erp.service.ActivityLogService;
    import com.erp.system.inventory.service.StockService;
    import com.erp.system.purchases.domain.PurchaseInvoice;
    import com.erp.system.purchases.domain.PurchaseInvoiceLine;
    import com.erp.system.purchases.dto.display.PurchaseInvoiceDisplayDto;
    import com.erp.system.purchases.dto.display.PurchaseInvoiceLineDisplayDto;
    import com.erp.system.purchases.dto.form.PurchaseInvoiceFormDto;
    import com.erp.system.purchases.repository.PurchaseInvoiceLineRepository;
    import com.erp.system.purchases.repository.PurchaseInvoiceRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class PurchaseInvoiceService {

        private static final String MODULE = "PURCHASES";

        private final PurchaseInvoiceRepository purchaseInvoiceRepository;
        private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
        private final NumberingService numberingService;
        private final ActivityLogService activityLogService;
        private final StockService stockService;
        private final AccountingPostingService accountingPostingService;
        private final AccountRepository accountRepository;
        private final JournalEntryRepository journalEntryRepository;

        @Transactional(readOnly = true)
        public List<PurchaseInvoiceDisplayDto> getAll() {
            return purchaseInvoiceRepository.findAllByOrderByIdDesc().stream()
                    .map(this::toDisplay)
                    .toList();
        }

        @Transactional(readOnly = true)
        public PurchaseInvoiceDisplayDto getById(Long id) {
            return toDisplay(loadPurchaseInvoice(id));
        }

        @Transactional
        public PurchaseInvoiceDisplayDto create(PurchaseInvoiceFormDto request) {
            PurchaseInvoice invoice = new PurchaseInvoice();
            applyForm(invoice, request);
            invoice.setInvoiceNumber(resolveNumber(request.getInvoiceNumber()));
            invoice.setStatus(TransactionStatus.DRAFT);
            invoice.setRemainingAmount(invoice.getTotalAmount());
            invoice = purchaseInvoiceRepository.save(invoice);
            activityLogService.log(MODULE, "CREATE", "PurchaseInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                    "Created purchase invoice " + invoice.getInvoiceNumber());
            return toDisplay(invoice);
        }

        @Transactional
        public PurchaseInvoiceDisplayDto update(Long id, PurchaseInvoiceFormDto request) {
            PurchaseInvoice invoice = loadPurchaseInvoice(id);
            if (invoice.getStatus() != TransactionStatus.DRAFT) {
                throw new BusinessException("Only draft invoices can be edited");
            }
            applyForm(invoice, request);
            if (request.getInvoiceNumber() != null && !request.getInvoiceNumber().isBlank()) {
                invoice.setInvoiceNumber(request.getInvoiceNumber().trim());
            }
            invoice.setRemainingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()).max(BigDecimal.ZERO));
            invoice = purchaseInvoiceRepository.save(invoice);
            activityLogService.log(MODULE, "UPDATE", "PurchaseInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                    "Updated purchase invoice " + invoice.getInvoiceNumber());
            return toDisplay(invoice);
        }

        @Transactional
        public PurchaseInvoiceDisplayDto approve(Long id, String actor) {
            PurchaseInvoice invoice = loadPurchaseInvoice(id);
            if (invoice.getStatus() == TransactionStatus.CANCELLED) {
                throw new BusinessException("Cancelled invoice cannot be approved");
            }
            if (invoice.getStatus() == TransactionStatus.APPROVED) {
                return toDisplay(invoice);
            }
            if (invoice.getWarehouseId() == null) {
                throw new BusinessException("Warehouse is required to approve purchase invoice");
            }

            List<PurchaseInvoiceLine> lines = purchaseInvoiceLineRepository.findByInvoiceIdOrderByIdAsc(invoice.getId());
            if (lines.isEmpty()) {
                throw new BusinessException("Cannot approve invoice without lines");
            }

            for (PurchaseInvoiceLine line : lines) {
                stockService.receiveStock(
                        line.getProductId(),
                        invoice.getWarehouseId(),
                        line.getQuantity(),
                        line.getUnitPrice(),
                        "PURCHASE_INVOICE",
                        invoice.getId(),
                        invoice.getInvoiceDate()
                );
            }

            Account inventoryAccount = accountRepository.findByCode("1300")
                    .orElseThrow(() -> new BusinessException("Inventory account 1300 not found"));
            Account taxAccount = accountRepository.findByCode("2210")
                    .orElseThrow(() -> new BusinessException("Tax account 2210 not found"));
            Account payableAccount = accountRepository.findByCode("2110")
                    .orElseThrow(() -> new BusinessException("Payables account 2110 not found"));

            BigDecimal taxAmount = invoice.getTaxAmount() == null ? BigDecimal.ZERO : invoice.getTaxAmount();
            BigDecimal inventoryAmount = invoice.getTotalAmount().subtract(taxAmount).max(BigDecimal.ZERO);
            String narrative = JournalPostingNarratives.entryHeader(
                    invoice.getNotes(),
                    JournalPostingNarratives.PURCHASE_INVOICE,
                    invoice.getInvoiceNumber()
            );

            List<AccountingPostingService.JournalLineDraft> journalLines = new ArrayList<>();
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(inventoryAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(narrative, inventoryAccount, true))
                    .debit(inventoryAmount)
                    .credit(BigDecimal.ZERO)
                    .build());
            if (taxAmount.compareTo(BigDecimal.ZERO) > 0) {
                journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                        .accountId(taxAccount.getId())
                        .description(JournalPostingNarratives.lineWithAccount(narrative, taxAccount, true))
                        .debit(taxAmount)
                        .credit(BigDecimal.ZERO)
                        .build());
            }
            journalLines.add(AccountingPostingService.JournalLineDraft.builder()
                    .accountId(payableAccount.getId())
                    .description(JournalPostingNarratives.lineWithAccount(narrative, payableAccount, false))
                    .debit(BigDecimal.ZERO)
                    .credit(invoice.getTotalAmount())
                    .build());

            JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                    invoice.getInvoiceDate(),
                    narrative,
                    "PURCHASE_INVOICE",
                    invoice.getId(),
                    actor,
                    journalLines
            );

            invoice.setJournalEntryId(journalEntry.getId());
            invoice.setStatus(TransactionStatus.APPROVED);
            invoice.setApprovedAt(LocalDateTime.now());
            invoice.setApprovedBy(actor);
            invoice = purchaseInvoiceRepository.save(invoice);

            activityLogService.log(MODULE, "APPROVE", "PurchaseInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                    "Approved purchase invoice " + invoice.getInvoiceNumber());
            return toDisplay(invoice);
        }

        @Transactional
        public PurchaseInvoiceDisplayDto cancel(Long id, String actor, String reason) {
            PurchaseInvoice invoice = loadPurchaseInvoice(id);
            if (invoice.getStatus() == TransactionStatus.CANCELLED) {
                return toDisplay(invoice);
            }
            if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException("Cannot cancel invoice with posted payments");
            }

            if (invoice.getJournalEntryId() != null) {
                JournalEntry original = journalEntryRepository.findById(invoice.getJournalEntryId())
                        .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", invoice.getJournalEntryId()));
                JournalEntry reversal = accountingPostingService.reverseJournal(
                        original,
                        actor,
                        reason,
                        LocalDate.now()
                );
                invoice.setCancellationJournalEntryId(reversal.getId());
            }
            invoice.setStatus(TransactionStatus.CANCELLED);
            invoice.setCancelledAt(LocalDateTime.now());
            invoice.setCancelledBy(actor);
            invoice = purchaseInvoiceRepository.save(invoice);
            activityLogService.log(MODULE, "CANCEL", "PurchaseInvoice", invoice.getId(), invoice.getInvoiceNumber(),
                    "Cancelled purchase invoice " + invoice.getInvoiceNumber());
            return toDisplay(invoice);
        }

        @Transactional
        public void delete(Long id) {
            PurchaseInvoice invoice = loadPurchaseInvoice(id);
            if (invoice.getStatus() != TransactionStatus.DRAFT) {
                throw new BusinessException("Only draft invoices can be deleted");
            }
            purchaseInvoiceRepository.delete(invoice);
            activityLogService.log(MODULE, "DELETE", "PurchaseInvoice", id, invoice.getInvoiceNumber(),
                    "Deleted purchase invoice " + invoice.getInvoiceNumber());
        }

        private String resolveNumber(String requested) {
            String normalized = requested == null ? null : requested.trim();
            if (normalized != null && !normalized.isEmpty()) {
                if (purchaseInvoiceRepository.existsByInvoiceNumberIgnoreCase(normalized)) {
                    throw new BusinessException("Invoice number already exists");
                }
                return normalized;
            }
            try {
                return numberingService.generateNextNumber("PURCHASE_INVOICE");
            } catch (Exception exception) {
                return "PINV-" + System.currentTimeMillis();
            }
        }

        private void applyForm(PurchaseInvoice invoice, PurchaseInvoiceFormDto request) {
            if (request.getDueDate().isBefore(request.getInvoiceDate())) {
                throw new BusinessException("Due date cannot be before invoice date");
            }
            invoice.setInvoiceDate(request.getInvoiceDate());
            invoice.setDueDate(request.getDueDate());
            invoice.setSupplierId(request.getSupplierId());
            invoice.setOrderId(request.getOrderId());
            invoice.setWarehouseId(request.getWarehouseId());
            invoice.setSubtotal(request.getSubtotal());
            invoice.setDiscountAmount(request.getDiscountAmount() == null ? BigDecimal.ZERO : request.getDiscountAmount());
            invoice.setTaxAmount(request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount());
            invoice.setTotalAmount(request.getTotalAmount());
            invoice.setNotes(request.getNotes());
        }

        private PurchaseInvoice loadPurchaseInvoice(Long id) {
            return purchaseInvoiceRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoice", id));
        }

        private PurchaseInvoiceDisplayDto toDisplay(PurchaseInvoice invoice) {
            List<PurchaseInvoiceLineDisplayDto> lines = purchaseInvoiceLineRepository.findByInvoiceIdOrderByIdAsc(invoice.getId())
                    .stream()
                    .map(line -> PurchaseInvoiceLineDisplayDto.builder()
                            .id(line.getId())
                            .invoiceId(line.getInvoiceId())
                            .productId(line.getProductId())
                            .description(line.getDescription())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .discountPercent(line.getDiscountPercent())
                            .taxPercent(line.getTaxPercent())
                            .lineTotal(line.getLineTotal())
                            .createdAt(line.getCreatedAt())
                            .updatedAt(line.getUpdatedAt())
                            .build())
                    .toList();
            return PurchaseInvoiceDisplayDto.builder()
                    .id(invoice.getId())
                    .invoiceNumber(invoice.getInvoiceNumber())
                    .invoiceDate(invoice.getInvoiceDate())
                    .dueDate(invoice.getDueDate())
                    .supplierId(invoice.getSupplierId())
                    .orderId(invoice.getOrderId())
                    .warehouseId(invoice.getWarehouseId())
                    .status(invoice.getStatus())
                    .subtotal(invoice.getSubtotal())
                    .discountAmount(invoice.getDiscountAmount())
                    .taxAmount(invoice.getTaxAmount())
                    .totalAmount(invoice.getTotalAmount())
                    .paidAmount(invoice.getPaidAmount())
                    .remainingAmount(invoice.getRemainingAmount())
                    .notes(invoice.getNotes())
                    .journalEntryId(invoice.getJournalEntryId())
                    .cancellationJournalEntryId(invoice.getCancellationJournalEntryId())
                    .approvedAt(invoice.getApprovedAt())
                    .approvedBy(invoice.getApprovedBy())
                    .cancelledAt(invoice.getCancelledAt())
                    .cancelledBy(invoice.getCancelledBy())
                    .lines(lines)
                    .createdAt(invoice.getCreatedAt())
                    .updatedAt(invoice.getUpdatedAt())
                    .build();
        }
    }
    """,
)

w(
    "purchases/controller/PurchaseInvoiceController.java",
    """
    package com.erp.system.purchases.controller;

    import com.erp.system.common.dto.ApiResponse;
    import com.erp.system.purchases.dto.display.PurchaseInvoiceDisplayDto;
    import com.erp.system.purchases.dto.form.PurchaseInvoiceFormDto;
    import com.erp.system.purchases.service.PurchaseInvoiceService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/purchases/invoices")
    @RequiredArgsConstructor
    public class PurchaseInvoiceController {

        private final PurchaseInvoiceService purchaseInvoiceService;

        @GetMapping
        public ApiResponse<List<PurchaseInvoiceDisplayDto>> getAll() {
            return ApiResponse.success(purchaseInvoiceService.getAll());
        }

        @GetMapping("/{id}")
        public ApiResponse<PurchaseInvoiceDisplayDto> getById(@PathVariable Long id) {
            return ApiResponse.success(purchaseInvoiceService.getById(id));
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<PurchaseInvoiceDisplayDto> create(@Valid @RequestBody PurchaseInvoiceFormDto request) {
            return ApiResponse.success(purchaseInvoiceService.create(request));
        }

        @PutMapping("/{id}")
        public ApiResponse<PurchaseInvoiceDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseInvoiceFormDto request) {
            return ApiResponse.success(purchaseInvoiceService.update(id, request));
        }

        @PostMapping("/{id}/approve")
        public ApiResponse<PurchaseInvoiceDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
            return ApiResponse.success(purchaseInvoiceService.approve(id, actor));
        }

        @PostMapping("/{id}/cancel")
        public ApiResponse<PurchaseInvoiceDisplayDto> cancel(@PathVariable Long id,
                                                             @RequestParam String actor,
                                                             @RequestParam(required = false) String reason) {
            return ApiResponse.success(purchaseInvoiceService.cancel(id, actor, reason));
        }

        @DeleteMapping("/{id}")
        public ApiResponse<Void> delete(@PathVariable Long id) {
            purchaseInvoiceService.delete(id);
            return ApiResponse.success(null);
        }
    }
    """,
)

w(
    "purchases/domain/SupplierPayment.java",
    """
    package com.erp.system.purchases.domain;

    import com.erp.system.common.entity.BaseEntity;
    import com.erp.system.common.enums.TransactionStatus;
    import jakarta.persistence.*;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.LocalDate;

    @Entity
    @Table(name = "supplier_payments", schema = "erp_system")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class SupplierPayment extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "payment_number", nullable = false, length = 50, unique = true)
        private String paymentNumber;

        @Column(name = "payment_date", nullable = false)
        private LocalDate paymentDate;

        @Column(name = "supplier_id", nullable = false)
        private Long supplierId;

        @Column(name = "invoice_id")
        private Long invoiceId;

        @Column(name = "amount", nullable = false, precision = 19, scale = 2)
        private BigDecimal amount;

        @Column(name = "payment_method", nullable = false, length = 30)
        private String paymentMethod;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private TransactionStatus status = TransactionStatus.DRAFT;

        @Column(name = "notes", length = 500)
        private String notes;

        @Column(name = "journal_entry_id")
        private Long journalEntryId;
    }
    """,
)

w(
    "purchases/repository/SupplierPaymentRepository.java",
    """
    package com.erp.system.purchases.repository;

    import com.erp.system.common.enums.TransactionStatus;
    import com.erp.system.purchases.domain.SupplierPayment;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;

    public interface SupplierPaymentRepository extends JpaRepository<SupplierPayment, Long> {
        List<SupplierPayment> findAllByOrderByIdDesc();
        List<SupplierPayment> findByStatusOrderByIdDesc(TransactionStatus status);
        boolean existsByPaymentNumberIgnoreCase(String paymentNumber);
    }
    """,
)

w(
    "purchases/dto/form/SupplierPaymentFormDto.java",
    """
    package com.erp.system.purchases.dto.form;

    import jakarta.validation.constraints.DecimalMin;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.LocalDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class SupplierPaymentFormDto {
        @Size(max = 50)
        private String paymentNumber;

        @NotNull
        private LocalDate paymentDate;

        @NotNull
        private Long supplierId;

        private Long invoiceId;

        @NotNull
        @DecimalMin("0.01")
        private BigDecimal amount;

        @NotBlank
        @Size(max = 30)
        private String paymentMethod;

        @Size(max = 500)
        private String notes;
    }
    """,
)

w(
    "purchases/dto/display/SupplierPaymentDisplayDto.java",
    """
    package com.erp.system.purchases.dto.display;

    import com.erp.system.common.enums.TransactionStatus;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.Instant;
    import java.time.LocalDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class SupplierPaymentDisplayDto {
        private Long id;
        private String paymentNumber;
        private LocalDate paymentDate;
        private Long supplierId;
        private Long invoiceId;
        private BigDecimal amount;
        private String paymentMethod;
        private TransactionStatus status;
        private String notes;
        private Long journalEntryId;
        private Instant createdAt;
        private Instant updatedAt;
    }
    """,
)

w(
    "purchases/service/SupplierPaymentService.java",
    """
    package com.erp.system.purchases.service;

    import com.erp.system.accounting.domain.Account;
    import com.erp.system.accounting.domain.JournalEntry;
    import com.erp.system.accounting.repository.AccountRepository;
    import com.erp.system.accounting.repository.JournalEntryRepository;
    import com.erp.system.accounting.service.AccountingPostingService;
    import com.erp.system.accounting.support.JournalPostingNarratives;
    import com.erp.system.common.enums.TransactionStatus;
    import com.erp.system.common.exception.BusinessException;
    import com.erp.system.common.exception.ResourceNotFoundException;
    import com.erp.system.common.service.NumberingService;
    import com.erp.system.erp.service.ActivityLogService;
    import com.erp.system.purchases.domain.PurchaseInvoice;
    import com.erp.system.purchases.domain.SupplierPayment;
    import com.erp.system.purchases.dto.display.SupplierPaymentDisplayDto;
    import com.erp.system.purchases.dto.form.SupplierPaymentFormDto;
    import com.erp.system.purchases.repository.PurchaseInvoiceRepository;
    import com.erp.system.purchases.repository.SupplierPaymentRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class SupplierPaymentService {

        private static final String MODULE = "PURCHASES";

        private final SupplierPaymentRepository supplierPaymentRepository;
        private final PurchaseInvoiceRepository purchaseInvoiceRepository;
        private final NumberingService numberingService;
        private final ActivityLogService activityLogService;
        private final AccountRepository accountRepository;
        private final AccountingPostingService accountingPostingService;
        private final JournalEntryRepository journalEntryRepository;

        @Transactional(readOnly = true)
        public List<SupplierPaymentDisplayDto> getAll() {
            return supplierPaymentRepository.findAllByOrderByIdDesc().stream()
                    .map(this::toDisplay)
                    .toList();
        }

        @Transactional(readOnly = true)
        public SupplierPaymentDisplayDto getById(Long id) {
            return toDisplay(loadSupplierPayment(id));
        }

        @Transactional
        public SupplierPaymentDisplayDto create(SupplierPaymentFormDto request) {
            SupplierPayment payment = new SupplierPayment();
            applyForm(payment, request);
            payment.setPaymentNumber(resolveNumber(request.getPaymentNumber()));
            payment.setStatus(TransactionStatus.DRAFT);
            payment = supplierPaymentRepository.save(payment);
            activityLogService.log(MODULE, "CREATE", "SupplierPayment", payment.getId(), payment.getPaymentNumber(),
                    "Created supplier payment " + payment.getPaymentNumber());
            return toDisplay(payment);
        }

        @Transactional
        public SupplierPaymentDisplayDto update(Long id, SupplierPaymentFormDto request) {
            SupplierPayment payment = loadSupplierPayment(id);
            if (payment.getStatus() != TransactionStatus.DRAFT) {
                throw new BusinessException("Only draft payments can be edited");
            }
            applyForm(payment, request);
            payment = supplierPaymentRepository.save(payment);
            activityLogService.log(MODULE, "UPDATE", "SupplierPayment", payment.getId(), payment.getPaymentNumber(),
                    "Updated supplier payment " + payment.getPaymentNumber());
            return toDisplay(payment);
        }

        @Transactional
        public SupplierPaymentDisplayDto approve(Long id, String actor) {
            SupplierPayment payment = loadSupplierPayment(id);
            if (payment.getStatus() == TransactionStatus.CANCELLED) {
                throw new BusinessException("Cancelled payment cannot be approved");
            }
            if (payment.getStatus() == TransactionStatus.APPROVED) {
                return toDisplay(payment);
            }

            Account apAccount = accountRepository.findByCode("2110")
                    .orElseThrow(() -> new BusinessException("Accounts payable account 2110 not found"));
            Account cashAccount = resolveCashBankAccount();
            String narrative = JournalPostingNarratives.entryHeader(
                    payment.getNotes(),
                    JournalPostingNarratives.PAYMENT_BOND,
                    payment.getPaymentNumber()
            );

            JournalEntry journalEntry = accountingPostingService.createPostedJournal(
                    payment.getPaymentDate(),
                    narrative,
                    "SUPPLIER_PAYMENT",
                    payment.getId(),
                    actor,
                    List.of(
                            AccountingPostingService.JournalLineDraft.builder()
                                    .accountId(apAccount.getId())
                                    .description(JournalPostingNarratives.lineWithAccount(narrative, apAccount, true))
                                    .debit(payment.getAmount())
                                    .credit(BigDecimal.ZERO)
                                    .build(),
                            AccountingPostingService.JournalLineDraft.builder()
                                    .accountId(cashAccount.getId())
                                    .description(JournalPostingNarratives.lineWithAccount(narrative, cashAccount, false))
                                    .debit(BigDecimal.ZERO)
                                    .credit(payment.getAmount())
                                    .build()
                    )
            );

            payment.setStatus(TransactionStatus.APPROVED);
            payment.setJournalEntryId(journalEntry.getId());
            payment = supplierPaymentRepository.save(payment);

            if (payment.getInvoiceId() != null) {
                PurchaseInvoice invoice = purchaseInvoiceRepository.findById(payment.getInvoiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("PurchaseInvoice", payment.getInvoiceId()));
                invoice.setPaidAmount(invoice.getPaidAmount().add(payment.getAmount()));
                invoice.setRemainingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()).max(BigDecimal.ZERO));
                purchaseInvoiceRepository.save(invoice);
            }

            activityLogService.log(MODULE, "APPROVE", "SupplierPayment", payment.getId(), payment.getPaymentNumber(),
                    "Approved supplier payment " + payment.getPaymentNumber());
            return toDisplay(payment);
        }

        @Transactional
        public SupplierPaymentDisplayDto cancel(Long id, String actor, String reason) {
            SupplierPayment payment = loadSupplierPayment(id);
            if (payment.getStatus() == TransactionStatus.CANCELLED) {
                return toDisplay(payment);
            }
            if (payment.getJournalEntryId() != null) {
                JournalEntry original = journalEntryRepository.findById(payment.getJournalEntryId())
                        .orElseThrow(() -> new ResourceNotFoundException("JournalEntry", payment.getJournalEntryId()));
                accountingPostingService.reverseJournal(original, actor, reason, LocalDate.now());
            }
            payment.setStatus(TransactionStatus.CANCELLED);
            payment = supplierPaymentRepository.save(payment);
            activityLogService.log(MODULE, "CANCEL", "SupplierPayment", payment.getId(), payment.getPaymentNumber(),
                    "Cancelled supplier payment " + payment.getPaymentNumber());
            return toDisplay(payment);
        }

        @Transactional
        public void delete(Long id) {
            SupplierPayment payment = loadSupplierPayment(id);
            if (payment.getStatus() != TransactionStatus.DRAFT) {
                throw new BusinessException("Only draft payments can be deleted");
            }
            supplierPaymentRepository.delete(payment);
            activityLogService.log(MODULE, "DELETE", "SupplierPayment", id, payment.getPaymentNumber(),
                    "Deleted supplier payment " + payment.getPaymentNumber());
        }

        private Account resolveCashBankAccount() {
            for (String code : List.of("1010", "1020", "1110")) {
                var account = accountRepository.findByCode(code);
                if (account.isPresent()) {
                    return account.get();
                }
            }
            throw new BusinessException("Cash/bank account not found (tried 1010, 1020, 1110)");
        }

        private String resolveNumber(String requested) {
            String normalized = requested == null ? null : requested.trim();
            if (normalized != null && !normalized.isEmpty()) {
                if (supplierPaymentRepository.existsByPaymentNumberIgnoreCase(normalized)) {
                    throw new BusinessException("Payment number already exists");
                }
                return normalized;
            }
            try {
                return numberingService.generateNextNumber("SUPPLIER_PAYMENT");
            } catch (Exception exception) {
                return "SP-" + System.currentTimeMillis();
            }
        }

        private void applyForm(SupplierPayment payment, SupplierPaymentFormDto request) {
            payment.setPaymentDate(request.getPaymentDate());
            payment.setSupplierId(request.getSupplierId());
            payment.setInvoiceId(request.getInvoiceId());
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod(request.getPaymentMethod().trim());
            payment.setNotes(request.getNotes());
        }

        private SupplierPayment loadSupplierPayment(Long id) {
            return supplierPaymentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("SupplierPayment", id));
        }

        private SupplierPaymentDisplayDto toDisplay(SupplierPayment payment) {
            return SupplierPaymentDisplayDto.builder()
                    .id(payment.getId())
                    .paymentNumber(payment.getPaymentNumber())
                    .paymentDate(payment.getPaymentDate())
                    .supplierId(payment.getSupplierId())
                    .invoiceId(payment.getInvoiceId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .status(payment.getStatus())
                    .notes(payment.getNotes())
                    .journalEntryId(payment.getJournalEntryId())
                    .createdAt(payment.getCreatedAt())
                    .updatedAt(payment.getUpdatedAt())
                    .build();
        }
    }
    """,
)

w(
    "purchases/controller/SupplierPaymentController.java",
    """
    package com.erp.system.purchases.controller;

    import com.erp.system.common.dto.ApiResponse;
    import com.erp.system.purchases.dto.display.SupplierPaymentDisplayDto;
    import com.erp.system.purchases.dto.form.SupplierPaymentFormDto;
    import com.erp.system.purchases.service.SupplierPaymentService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/purchases/payments")
    @RequiredArgsConstructor
    public class SupplierPaymentController {

        private final SupplierPaymentService supplierPaymentService;

        @GetMapping
        public ApiResponse<List<SupplierPaymentDisplayDto>> getAll() {
            return ApiResponse.success(supplierPaymentService.getAll());
        }

        @GetMapping("/{id}")
        public ApiResponse<SupplierPaymentDisplayDto> getById(@PathVariable Long id) {
            return ApiResponse.success(supplierPaymentService.getById(id));
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<SupplierPaymentDisplayDto> create(@Valid @RequestBody SupplierPaymentFormDto request) {
            return ApiResponse.success(supplierPaymentService.create(request));
        }

        @PutMapping("/{id}")
        public ApiResponse<SupplierPaymentDisplayDto> update(@PathVariable Long id, @Valid @RequestBody SupplierPaymentFormDto request) {
            return ApiResponse.success(supplierPaymentService.update(id, request));
        }

        @PostMapping("/{id}/approve")
        public ApiResponse<SupplierPaymentDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
            return ApiResponse.success(supplierPaymentService.approve(id, actor));
        }

        @PostMapping("/{id}/cancel")
        public ApiResponse<SupplierPaymentDisplayDto> cancel(@PathVariable Long id,
                                                             @RequestParam String actor,
                                                             @RequestParam(required = false) String reason) {
            return ApiResponse.success(supplierPaymentService.cancel(id, actor, reason));
        }

        @DeleteMapping("/{id}")
        public ApiResponse<Void> delete(@PathVariable Long id) {
            supplierPaymentService.delete(id);
            return ApiResponse.success(null);
        }
    }
    """,
)

# Add approve/cancel operations to order/return controllers and services
w(
    "purchases/service/PurchaseOrderService.java",
    """
    package com.erp.system.purchases.service;

    import com.erp.system.common.enums.TransactionStatus;
    import com.erp.system.common.exception.BusinessException;
    import com.erp.system.common.exception.ResourceNotFoundException;
    import com.erp.system.erp.service.ActivityLogService;
    import com.erp.system.purchases.domain.PurchaseOrder;
    import com.erp.system.purchases.dto.display.PurchaseOrderDisplayDto;
    import com.erp.system.purchases.dto.form.PurchaseOrderFormDto;
    import com.erp.system.purchases.repository.PurchaseOrderRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.math.BigDecimal;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class PurchaseOrderService {

        private static final String MODULE = "PURCHASES";

        private final PurchaseOrderRepository purchaseOrderRepository;
        private final ActivityLogService activityLogService;

        @Transactional(readOnly = true)
        public List<PurchaseOrderDisplayDto> getAll() {
            return purchaseOrderRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
        }

        @Transactional(readOnly = true)
        public PurchaseOrderDisplayDto getById(Long id) {
            return toDisplay(loadPurchaseOrder(id));
        }

        @Transactional
        public PurchaseOrderDisplayDto create(PurchaseOrderFormDto request) {
            PurchaseOrder purchaseOrder = new PurchaseOrder();
            applyForm(purchaseOrder, request);
            purchaseOrder.setStatus(TransactionStatus.DRAFT);
            purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
            activityLogService.log(MODULE, "CREATE", "PurchaseOrder", purchaseOrder.getId(), purchaseOrder.getOrderNumber(),
                    "Created purchase order " + purchaseOrder.getOrderNumber());
            return toDisplay(purchaseOrder);
        }

        @Transactional
        public PurchaseOrderDisplayDto update(Long id, PurchaseOrderFormDto request) {
            PurchaseOrder purchaseOrder = loadPurchaseOrder(id);
            if (purchaseOrder.getStatus() != TransactionStatus.DRAFT) {
                throw new BusinessException("Only draft orders can be edited");
            }
            applyForm(purchaseOrder, request);
            purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
            activityLogService.log(MODULE, "UPDATE", "PurchaseOrder", purchaseOrder.getId(), purchaseOrder.getOrderNumber(),
                    "Updated purchase order " + purchaseOrder.getOrderNumber());
            return toDisplay(purchaseOrder);
        }

        @Transactional
        public PurchaseOrderDisplayDto approve(Long id, String actor) {
            PurchaseOrder purchaseOrder = loadPurchaseOrder(id);
            if (purchaseOrder.getStatus() == TransactionStatus.CANCELLED) {
                throw new BusinessException("Cancelled order cannot be approved");
            }
            purchaseOrder.setStatus(TransactionStatus.APPROVED);
            purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
            activityLogService.log(MODULE, "APPROVE", "PurchaseOrder", purchaseOrder.getId(), purchaseOrder.getOrderNumber(),
                    "Approved purchase order " + purchaseOrder.getOrderNumber());
            return toDisplay(purchaseOrder);
        }

        @Transactional
        public PurchaseOrderDisplayDto cancel(Long id, String actor, String reason) {
            PurchaseOrder purchaseOrder = loadPurchaseOrder(id);
            purchaseOrder.setStatus(TransactionStatus.CANCELLED);
            purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
            activityLogService.log(MODULE, "CANCEL", "PurchaseOrder", purchaseOrder.getId(), purchaseOrder.getOrderNumber(),
                    "Cancelled purchase order " + purchaseOrder.getOrderNumber());
            return toDisplay(purchaseOrder);
        }

        @Transactional
        public void delete(Long id) {
            PurchaseOrder purchaseOrder = loadPurchaseOrder(id);
            if (purchaseOrder.getStatus() != TransactionStatus.DRAFT) {
                throw new BusinessException("Only draft orders can be deleted");
            }
            purchaseOrderRepository.delete(purchaseOrder);
            activityLogService.log(MODULE, "DELETE", "PurchaseOrder", id, purchaseOrder.getOrderNumber(),
                    "Deleted purchase order " + purchaseOrder.getOrderNumber());
        }

        private PurchaseOrder loadPurchaseOrder(Long id) {
            return purchaseOrderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        }

        private void applyForm(PurchaseOrder purchaseOrder, PurchaseOrderFormDto request) {
            purchaseOrder.setOrderNumber(request.getOrderNumber());
            purchaseOrder.setOrderDate(request.getOrderDate());
            purchaseOrder.setSupplierId(request.getSupplierId());
            purchaseOrder.setWarehouseId(request.getWarehouseId());
            purchaseOrder.setSubtotal(request.getSubtotal());
            purchaseOrder.setDiscountAmount(request.getDiscountAmount() == null ? BigDecimal.ZERO : request.getDiscountAmount());
            purchaseOrder.setTaxAmount(request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount());
            purchaseOrder.setTotalAmount(request.getTotalAmount());
            purchaseOrder.setNotes(request.getNotes());
        }

        private PurchaseOrderDisplayDto toDisplay(PurchaseOrder purchaseOrder) {
            return PurchaseOrderDisplayDto.builder()
                    .id(purchaseOrder.getId())
                    .orderNumber(purchaseOrder.getOrderNumber())
                    .orderDate(purchaseOrder.getOrderDate())
                    .supplierId(purchaseOrder.getSupplierId())
                    .warehouseId(purchaseOrder.getWarehouseId())
                    .status(purchaseOrder.getStatus())
                    .subtotal(purchaseOrder.getSubtotal())
                    .discountAmount(purchaseOrder.getDiscountAmount())
                    .taxAmount(purchaseOrder.getTaxAmount())
                    .totalAmount(purchaseOrder.getTotalAmount())
                    .notes(purchaseOrder.getNotes())
                    .createdAt(purchaseOrder.getCreatedAt())
                    .updatedAt(purchaseOrder.getUpdatedAt())
                    .build();
        }
    }
    """,
)

w(
    "purchases/controller/PurchaseOrderController.java",
    """
    package com.erp.system.purchases.controller;

    import com.erp.system.common.dto.ApiResponse;
    import com.erp.system.purchases.dto.display.PurchaseOrderDisplayDto;
    import com.erp.system.purchases.dto.form.PurchaseOrderFormDto;
    import com.erp.system.purchases.service.PurchaseOrderService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/purchases/orders")
    @RequiredArgsConstructor
    public class PurchaseOrderController {

        private final PurchaseOrderService purchaseOrderService;

        @GetMapping
        public ApiResponse<List<PurchaseOrderDisplayDto>> getAll() {
            return ApiResponse.success(purchaseOrderService.getAll());
        }

        @GetMapping("/{id}")
        public ApiResponse<PurchaseOrderDisplayDto> getById(@PathVariable Long id) {
            return ApiResponse.success(purchaseOrderService.getById(id));
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<PurchaseOrderDisplayDto> create(@Valid @RequestBody PurchaseOrderFormDto request) {
            return ApiResponse.success(purchaseOrderService.create(request));
        }

        @PutMapping("/{id}")
        public ApiResponse<PurchaseOrderDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderFormDto request) {
            return ApiResponse.success(purchaseOrderService.update(id, request));
        }

        @PostMapping("/{id}/approve")
        public ApiResponse<PurchaseOrderDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
            return ApiResponse.success(purchaseOrderService.approve(id, actor));
        }

        @PostMapping("/{id}/cancel")
        public ApiResponse<PurchaseOrderDisplayDto> cancel(@PathVariable Long id,
                                                           @RequestParam String actor,
                                                           @RequestParam(required = false) String reason) {
            return ApiResponse.success(purchaseOrderService.cancel(id, actor, reason));
        }

        @DeleteMapping("/{id}")
        public ApiResponse<Void> delete(@PathVariable Long id) {
            purchaseOrderService.delete(id);
            return ApiResponse.success(null);
        }
    }
    """,
)

w(
    "purchases/service/PurchaseReturnService.java",
    """
    package com.erp.system.purchases.service;

    import com.erp.system.common.enums.TransactionStatus;
    import com.erp.system.common.exception.BusinessException;
    import com.erp.system.common.exception.ResourceNotFoundException;
    import com.erp.system.erp.service.ActivityLogService;
    import com.erp.system.purchases.domain.PurchaseReturn;
    import com.erp.system.purchases.dto.display.PurchaseReturnDisplayDto;
    import com.erp.system.purchases.dto.form.PurchaseReturnFormDto;
    import com.erp.system.purchases.repository.PurchaseReturnRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.math.BigDecimal;
    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class PurchaseReturnService {

        private static final String MODULE = "PURCHASES";

        private final PurchaseReturnRepository purchaseReturnRepository;
        private final ActivityLogService activityLogService;

        @Transactional(readOnly = true)
        public List<PurchaseReturnDisplayDto> getAll() {
            return purchaseReturnRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
        }

        @Transactional(readOnly = true)
        public PurchaseReturnDisplayDto getById(Long id) {
            return toDisplay(loadPurchaseReturn(id));
        }

        @Transactional
        public PurchaseReturnDisplayDto create(PurchaseReturnFormDto request) {
            PurchaseReturn purchaseReturn = new PurchaseReturn();
            applyForm(purchaseReturn, request);
            purchaseReturn.setStatus(TransactionStatus.DRAFT);
            purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
            activityLogService.log(MODULE, "CREATE", "PurchaseReturn", purchaseReturn.getId(), purchaseReturn.getReturnNumber(),
                    "Created purchase return " + purchaseReturn.getReturnNumber());
            return toDisplay(purchaseReturn);
        }

        @Transactional
        public PurchaseReturnDisplayDto update(Long id, PurchaseReturnFormDto request) {
            PurchaseReturn purchaseReturn = loadPurchaseReturn(id);
            if (purchaseReturn.getStatus() != TransactionStatus.DRAFT) {
                throw new BusinessException("Only draft returns can be edited");
            }
            applyForm(purchaseReturn, request);
            purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
            activityLogService.log(MODULE, "UPDATE", "PurchaseReturn", purchaseReturn.getId(), purchaseReturn.getReturnNumber(),
                    "Updated purchase return " + purchaseReturn.getReturnNumber());
            return toDisplay(purchaseReturn);
        }

        @Transactional
        public PurchaseReturnDisplayDto approve(Long id, String actor) {
            PurchaseReturn purchaseReturn = loadPurchaseReturn(id);
            if (purchaseReturn.getStatus() == TransactionStatus.CANCELLED) {
                throw new BusinessException("Cancelled return cannot be approved");
            }
            purchaseReturn.setStatus(TransactionStatus.APPROVED);
            purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
            activityLogService.log(MODULE, "APPROVE", "PurchaseReturn", purchaseReturn.getId(), purchaseReturn.getReturnNumber(),
                    "Approved purchase return " + purchaseReturn.getReturnNumber());
            return toDisplay(purchaseReturn);
        }

        @Transactional
        public PurchaseReturnDisplayDto cancel(Long id, String actor, String reason) {
            PurchaseReturn purchaseReturn = loadPurchaseReturn(id);
            purchaseReturn.setStatus(TransactionStatus.CANCELLED);
            purchaseReturn = purchaseReturnRepository.save(purchaseReturn);
            activityLogService.log(MODULE, "CANCEL", "PurchaseReturn", purchaseReturn.getId(), purchaseReturn.getReturnNumber(),
                    "Cancelled purchase return " + purchaseReturn.getReturnNumber());
            return toDisplay(purchaseReturn);
        }

        @Transactional
        public void delete(Long id) {
            PurchaseReturn purchaseReturn = loadPurchaseReturn(id);
            if (purchaseReturn.getStatus() != TransactionStatus.DRAFT) {
                throw new BusinessException("Only draft returns can be deleted");
            }
            purchaseReturnRepository.delete(purchaseReturn);
            activityLogService.log(MODULE, "DELETE", "PurchaseReturn", id, purchaseReturn.getReturnNumber(),
                    "Deleted purchase return " + purchaseReturn.getReturnNumber());
        }

        private PurchaseReturn loadPurchaseReturn(Long id) {
            return purchaseReturnRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("PurchaseReturn", id));
        }

        private void applyForm(PurchaseReturn purchaseReturn, PurchaseReturnFormDto request) {
            purchaseReturn.setReturnNumber(request.getReturnNumber());
            purchaseReturn.setReturnDate(request.getReturnDate());
            purchaseReturn.setSupplierId(request.getSupplierId());
            purchaseReturn.setInvoiceId(request.getInvoiceId());
            purchaseReturn.setWarehouseId(request.getWarehouseId());
            purchaseReturn.setSubtotal(request.getSubtotal());
            purchaseReturn.setTaxAmount(request.getTaxAmount() == null ? BigDecimal.ZERO : request.getTaxAmount());
            purchaseReturn.setTotalAmount(request.getTotalAmount());
            purchaseReturn.setNotes(request.getNotes());
        }

        private PurchaseReturnDisplayDto toDisplay(PurchaseReturn purchaseReturn) {
            return PurchaseReturnDisplayDto.builder()
                    .id(purchaseReturn.getId())
                    .returnNumber(purchaseReturn.getReturnNumber())
                    .returnDate(purchaseReturn.getReturnDate())
                    .supplierId(purchaseReturn.getSupplierId())
                    .invoiceId(purchaseReturn.getInvoiceId())
                    .warehouseId(purchaseReturn.getWarehouseId())
                    .status(purchaseReturn.getStatus())
                    .subtotal(purchaseReturn.getSubtotal())
                    .taxAmount(purchaseReturn.getTaxAmount())
                    .totalAmount(purchaseReturn.getTotalAmount())
                    .notes(purchaseReturn.getNotes())
                    .journalEntryId(purchaseReturn.getJournalEntryId())
                    .createdAt(purchaseReturn.getCreatedAt())
                    .updatedAt(purchaseReturn.getUpdatedAt())
                    .build();
        }
    }
    """,
)

w(
    "purchases/controller/PurchaseReturnController.java",
    """
    package com.erp.system.purchases.controller;

    import com.erp.system.common.dto.ApiResponse;
    import com.erp.system.purchases.dto.display.PurchaseReturnDisplayDto;
    import com.erp.system.purchases.dto.form.PurchaseReturnFormDto;
    import com.erp.system.purchases.service.PurchaseReturnService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/purchases/returns")
    @RequiredArgsConstructor
    public class PurchaseReturnController {

        private final PurchaseReturnService purchaseReturnService;

        @GetMapping
        public ApiResponse<List<PurchaseReturnDisplayDto>> getAll() {
            return ApiResponse.success(purchaseReturnService.getAll());
        }

        @GetMapping("/{id}")
        public ApiResponse<PurchaseReturnDisplayDto> getById(@PathVariable Long id) {
            return ApiResponse.success(purchaseReturnService.getById(id));
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<PurchaseReturnDisplayDto> create(@Valid @RequestBody PurchaseReturnFormDto request) {
            return ApiResponse.success(purchaseReturnService.create(request));
        }

        @PutMapping("/{id}")
        public ApiResponse<PurchaseReturnDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PurchaseReturnFormDto request) {
            return ApiResponse.success(purchaseReturnService.update(id, request));
        }

        @PostMapping("/{id}/approve")
        public ApiResponse<PurchaseReturnDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
            return ApiResponse.success(purchaseReturnService.approve(id, actor));
        }

        @PostMapping("/{id}/cancel")
        public ApiResponse<PurchaseReturnDisplayDto> cancel(@PathVariable Long id,
                                                            @RequestParam String actor,
                                                            @RequestParam(required = false) String reason) {
            return ApiResponse.success(purchaseReturnService.cancel(id, actor, reason));
        }

        @DeleteMapping("/{id}")
        public ApiResponse<Void> delete(@PathVariable Long id) {
            purchaseReturnService.delete(id);
            return ApiResponse.success(null);
        }
    }
    """,
)

# -----------------------------------------------------------------------------
# HR
# -----------------------------------------------------------------------------
add_standard_entity(
    module="hr",
    module_code="HR",
    entity="Department",
    table="departments",
    route="departments",
    domain_fields="""
        @Column(name = "code", nullable = false, length = 30, unique = true)
        private String code;

        @Column(name = "name_en", nullable = false, length = 150)
        private String nameEn;

        @Column(name = "name_ar", length = 150)
        private String nameAr;

        @Column(name = "manager_id")
        private Long managerId;

        @Column(name = "is_active", nullable = false)
        @Builder.Default
        private boolean active = true;
    """,
    form_fields="""
        @NotBlank
        @Size(max = 30)
        private String code;

        @NotBlank
        @Size(max = 150)
        private String nameEn;

        @Size(max = 150)
        private String nameAr;

        private Long managerId;
        private Boolean active;
    """,
    display_fields="""
        private String code;
        private String nameEn;
        private String nameAr;
        private Long managerId;
        private boolean active;
    """,
    apply_form_lines="""
                department.setCode(request.getCode().trim());
                department.setNameEn(request.getNameEn().trim());
                department.setNameAr(request.getNameAr());
                department.setManagerId(request.getManagerId());
                department.setActive(request.getActive() == null || request.getActive());
    """,
    to_display_lines="""
                        .code(department.getCode())
                        .nameEn(department.getNameEn())
                        .nameAr(department.getNameAr())
                        .managerId(department.getManagerId())
                        .active(department.isActive())
    """,
)

add_standard_entity(
    module="hr",
    module_code="HR",
    entity="Employee",
    table="employees",
    route="employees",
    domain_fields="""
        @Column(name = "employee_code", nullable = false, length = 50, unique = true)
        private String employeeCode;

        @Column(name = "full_name_en", nullable = false, length = 200)
        private String fullNameEn;

        @Column(name = "full_name_ar", length = 200)
        private String fullNameAr;

        @Column(name = "email", length = 190)
        private String email;

        @Column(name = "phone", length = 30)
        private String phone;

        @Column(name = "department_id")
        private Long departmentId;

        @Column(name = "job_title", length = 150)
        private String jobTitle;

        @Column(name = "hire_date")
        private LocalDate hireDate;

        @Column(name = "basic_salary", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal basicSalary = BigDecimal.ZERO;

        @Column(name = "is_active", nullable = false)
        @Builder.Default
        private boolean active = true;
    """,
    form_fields="""
        @NotBlank
        @Size(max = 50)
        private String employeeCode;

        @NotBlank
        @Size(max = 200)
        private String fullNameEn;

        @Size(max = 200)
        private String fullNameAr;

        @Email
        @Size(max = 190)
        private String email;

        @Size(max = 30)
        private String phone;

        private Long departmentId;

        @Size(max = 150)
        private String jobTitle;

        private LocalDate hireDate;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal basicSalary;

        private Boolean active;
    """,
    display_fields="""
        private String employeeCode;
        private String fullNameEn;
        private String fullNameAr;
        private String email;
        private String phone;
        private Long departmentId;
        private String jobTitle;
        private LocalDate hireDate;
        private BigDecimal basicSalary;
        private boolean active;
    """,
    apply_form_lines="""
                employee.setEmployeeCode(request.getEmployeeCode().trim());
                employee.setFullNameEn(request.getFullNameEn().trim());
                employee.setFullNameAr(request.getFullNameAr());
                employee.setEmail(request.getEmail());
                employee.setPhone(request.getPhone());
                employee.setDepartmentId(request.getDepartmentId());
                employee.setJobTitle(request.getJobTitle());
                employee.setHireDate(request.getHireDate());
                employee.setBasicSalary(request.getBasicSalary());
                employee.setActive(request.getActive() == null || request.getActive());
    """,
    to_display_lines="""
                        .employeeCode(employee.getEmployeeCode())
                        .fullNameEn(employee.getFullNameEn())
                        .fullNameAr(employee.getFullNameAr())
                        .email(employee.getEmail())
                        .phone(employee.getPhone())
                        .departmentId(employee.getDepartmentId())
                        .jobTitle(employee.getJobTitle())
                        .hireDate(employee.getHireDate())
                        .basicSalary(employee.getBasicSalary())
                        .active(employee.isActive())
    """,
    service_imports="import java.math.BigDecimal;",
)

add_standard_entity(
    module="hr",
    module_code="HR",
    entity="AttendanceRecord",
    table="attendance_records",
    route="attendance",
    domain_fields="""
        @Column(name = "employee_id", nullable = false)
        private Long employeeId;

        @Column(name = "attendance_date", nullable = false)
        private LocalDate attendanceDate;

        @Column(name = "check_in")
        private LocalTime checkIn;

        @Column(name = "check_out")
        private LocalTime checkOut;

        @Column(name = "status", nullable = false, length = 20)
        private String status;

        @Column(name = "notes", length = 300)
        private String notes;
    """,
    form_fields="""
        @NotNull
        private Long employeeId;

        @NotNull
        private LocalDate attendanceDate;

        private LocalTime checkIn;
        private LocalTime checkOut;

        @NotBlank
        @Size(max = 20)
        private String status;

        @Size(max = 300)
        private String notes;
    """,
    display_fields="""
        private Long employeeId;
        private LocalDate attendanceDate;
        private LocalTime checkIn;
        private LocalTime checkOut;
        private String status;
        private String notes;
    """,
    apply_form_lines="""
                attendanceRecord.setEmployeeId(request.getEmployeeId());
                attendanceRecord.setAttendanceDate(request.getAttendanceDate());
                attendanceRecord.setCheckIn(request.getCheckIn());
                attendanceRecord.setCheckOut(request.getCheckOut());
                attendanceRecord.setStatus(request.getStatus().trim());
                attendanceRecord.setNotes(request.getNotes());
    """,
    to_display_lines="""
                        .employeeId(attendanceRecord.getEmployeeId())
                        .attendanceDate(attendanceRecord.getAttendanceDate())
                        .checkIn(attendanceRecord.getCheckIn())
                        .checkOut(attendanceRecord.getCheckOut())
                        .status(attendanceRecord.getStatus())
                        .notes(attendanceRecord.getNotes())
    """,
)

add_standard_entity(
    module="hr",
    module_code="HR",
    entity="PayrollLine",
    table="payroll_lines",
    route="payroll-lines",
    domain_fields="""
        @Column(name = "payroll_id", nullable = false)
        private Long payrollId;

        @Column(name = "employee_id", nullable = false)
        private Long employeeId;

        @Column(name = "basic_salary", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal basicSalary = BigDecimal.ZERO;

        @Column(name = "allowances", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal allowances = BigDecimal.ZERO;

        @Column(name = "deductions", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal deductions = BigDecimal.ZERO;

        @Column(name = "net_salary", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal netSalary = BigDecimal.ZERO;
    """,
    form_fields="""
        @NotNull
        private Long payrollId;

        @NotNull
        private Long employeeId;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal basicSalary;

        @DecimalMin("0.0")
        private BigDecimal allowances;

        @DecimalMin("0.0")
        private BigDecimal deductions;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal netSalary;
    """,
    display_fields="""
        private Long payrollId;
        private Long employeeId;
        private BigDecimal basicSalary;
        private BigDecimal allowances;
        private BigDecimal deductions;
        private BigDecimal netSalary;
    """,
    apply_form_lines="""
                payrollLine.setPayrollId(request.getPayrollId());
                payrollLine.setEmployeeId(request.getEmployeeId());
                payrollLine.setBasicSalary(request.getBasicSalary());
                payrollLine.setAllowances(request.getAllowances() == null ? BigDecimal.ZERO : request.getAllowances());
                payrollLine.setDeductions(request.getDeductions() == null ? BigDecimal.ZERO : request.getDeductions());
                payrollLine.setNetSalary(request.getNetSalary());
    """,
    to_display_lines="""
                        .payrollId(payrollLine.getPayrollId())
                        .employeeId(payrollLine.getEmployeeId())
                        .basicSalary(payrollLine.getBasicSalary())
                        .allowances(payrollLine.getAllowances())
                        .deductions(payrollLine.getDeductions())
                        .netSalary(payrollLine.getNetSalary())
    """,
    repo_extra="""
            java.util.List<PayrollLine> findByPayrollIdOrderByIdAsc(Long payrollId);
    """,
    service_imports="import java.math.BigDecimal;",
)

add_standard_entity(
    module="hr",
    module_code="HR",
    entity="EmployeeDocument",
    table="employee_documents",
    route="documents",
    domain_fields="""
        @Column(name = "employee_id", nullable = false)
        private Long employeeId;

        @Column(name = "document_type", nullable = false, length = 50)
        private String documentType;

        @Column(name = "file_name", nullable = false, length = 255)
        private String fileName;

        @Column(name = "file_path", length = 500)
        private String filePath;

        @Column(name = "expiry_date")
        private LocalDate expiryDate;
    """,
    form_fields="""
        @NotNull
        private Long employeeId;

        @NotBlank
        @Size(max = 50)
        private String documentType;

        @NotBlank
        @Size(max = 255)
        private String fileName;

        @Size(max = 500)
        private String filePath;

        private LocalDate expiryDate;
    """,
    display_fields="""
        private Long employeeId;
        private String documentType;
        private String fileName;
        private String filePath;
        private LocalDate expiryDate;
    """,
    apply_form_lines="""
                employeeDocument.setEmployeeId(request.getEmployeeId());
                employeeDocument.setDocumentType(request.getDocumentType().trim());
                employeeDocument.setFileName(request.getFileName().trim());
                employeeDocument.setFilePath(request.getFilePath());
                employeeDocument.setExpiryDate(request.getExpiryDate());
    """,
    to_display_lines="""
                        .employeeId(employeeDocument.getEmployeeId())
                        .documentType(employeeDocument.getDocumentType())
                        .fileName(employeeDocument.getFileName())
                        .filePath(employeeDocument.getFilePath())
                        .expiryDate(employeeDocument.getExpiryDate())
    """,
)

w(
    "hr/domain/LeaveRequest.java",
    """
    package com.erp.system.hr.domain;

    import com.erp.system.common.entity.BaseEntity;
    import com.erp.system.common.enums.TransactionStatus;
    import jakarta.persistence.*;
    import lombok.*;

    import java.time.LocalDate;

    @Entity
    @Table(name = "leave_requests", schema = "erp_system")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class LeaveRequest extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "employee_id", nullable = false)
        private Long employeeId;

        @Column(name = "leave_type", nullable = false, length = 30)
        private String leaveType;

        @Column(name = "start_date", nullable = false)
        private LocalDate startDate;

        @Column(name = "end_date", nullable = false)
        private LocalDate endDate;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private TransactionStatus status = TransactionStatus.PENDING;

        @Column(name = "reason", length = 500)
        private String reason;
    }
    """,
)

w(
    "hr/repository/LeaveRequestRepository.java",
    """
    package com.erp.system.hr.repository;

    import com.erp.system.hr.domain.LeaveRequest;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;

    public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
        List<LeaveRequest> findAllByOrderByIdDesc();
    }
    """,
)

w(
    "hr/dto/form/LeaveRequestFormDto.java",
    """
    package com.erp.system.hr.dto.form;

    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.*;

    import java.time.LocalDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class LeaveRequestFormDto {
        @NotNull
        private Long employeeId;

        @NotBlank
        @Size(max = 30)
        private String leaveType;

        @NotNull
        private LocalDate startDate;

        @NotNull
        private LocalDate endDate;

        @Size(max = 500)
        private String reason;
    }
    """,
)

w(
    "hr/dto/display/LeaveRequestDisplayDto.java",
    """
    package com.erp.system.hr.dto.display;

    import com.erp.system.common.enums.TransactionStatus;
    import lombok.*;

    import java.time.Instant;
    import java.time.LocalDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class LeaveRequestDisplayDto {
        private Long id;
        private Long employeeId;
        private String leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private TransactionStatus status;
        private String reason;
        private Instant createdAt;
        private Instant updatedAt;
    }
    """,
)

w(
    "hr/service/LeaveRequestService.java",
    """
    package com.erp.system.hr.service;

    import com.erp.system.common.enums.TransactionStatus;
    import com.erp.system.common.exception.BusinessException;
    import com.erp.system.common.exception.ResourceNotFoundException;
    import com.erp.system.erp.service.ActivityLogService;
    import com.erp.system.hr.domain.LeaveRequest;
    import com.erp.system.hr.dto.display.LeaveRequestDisplayDto;
    import com.erp.system.hr.dto.form.LeaveRequestFormDto;
    import com.erp.system.hr.repository.LeaveRequestRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class LeaveRequestService {

        private static final String MODULE = "HR";

        private final LeaveRequestRepository leaveRequestRepository;
        private final ActivityLogService activityLogService;

        @Transactional(readOnly = true)
        public List<LeaveRequestDisplayDto> getAll() {
            return leaveRequestRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
        }

        @Transactional(readOnly = true)
        public LeaveRequestDisplayDto getById(Long id) {
            return toDisplay(loadLeaveRequest(id));
        }

        @Transactional
        public LeaveRequestDisplayDto create(LeaveRequestFormDto request) {
            LeaveRequest leaveRequest = new LeaveRequest();
            applyForm(leaveRequest, request);
            leaveRequest.setStatus(TransactionStatus.PENDING);
            leaveRequest = leaveRequestRepository.save(leaveRequest);
            activityLogService.log(MODULE, "CREATE", "LeaveRequest", leaveRequest.getId(), String.valueOf(leaveRequest.getId()),
                    "Created leave request " + leaveRequest.getId());
            return toDisplay(leaveRequest);
        }

        @Transactional
        public LeaveRequestDisplayDto update(Long id, LeaveRequestFormDto request) {
            LeaveRequest leaveRequest = loadLeaveRequest(id);
            if (leaveRequest.getStatus() == TransactionStatus.APPROVED) {
                throw new BusinessException("Approved leave request cannot be edited");
            }
            applyForm(leaveRequest, request);
            leaveRequest = leaveRequestRepository.save(leaveRequest);
            activityLogService.log(MODULE, "UPDATE", "LeaveRequest", leaveRequest.getId(), String.valueOf(leaveRequest.getId()),
                    "Updated leave request " + leaveRequest.getId());
            return toDisplay(leaveRequest);
        }

        @Transactional
        public LeaveRequestDisplayDto approve(Long id, String actor) {
            LeaveRequest leaveRequest = loadLeaveRequest(id);
            if (leaveRequest.getStatus() == TransactionStatus.CANCELLED) {
                throw new BusinessException("Cancelled leave request cannot be approved");
            }
            leaveRequest.setStatus(TransactionStatus.APPROVED);
            leaveRequest = leaveRequestRepository.save(leaveRequest);
            activityLogService.log(MODULE, "APPROVE", "LeaveRequest", leaveRequest.getId(), String.valueOf(leaveRequest.getId()),
                    "Approved leave request " + leaveRequest.getId());
            return toDisplay(leaveRequest);
        }

        @Transactional
        public LeaveRequestDisplayDto cancel(Long id, String actor, String reason) {
            LeaveRequest leaveRequest = loadLeaveRequest(id);
            leaveRequest.setStatus(TransactionStatus.CANCELLED);
            leaveRequest = leaveRequestRepository.save(leaveRequest);
            activityLogService.log(MODULE, "CANCEL", "LeaveRequest", leaveRequest.getId(), String.valueOf(leaveRequest.getId()),
                    "Cancelled leave request " + leaveRequest.getId());
            return toDisplay(leaveRequest);
        }

        @Transactional
        public void delete(Long id) {
            LeaveRequest leaveRequest = loadLeaveRequest(id);
            leaveRequestRepository.delete(leaveRequest);
            activityLogService.log(MODULE, "DELETE", "LeaveRequest", id, String.valueOf(id),
                    "Deleted leave request " + id);
        }

        private void applyForm(LeaveRequest leaveRequest, LeaveRequestFormDto request) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new BusinessException("End date cannot be before start date");
            }
            leaveRequest.setEmployeeId(request.getEmployeeId());
            leaveRequest.setLeaveType(request.getLeaveType().trim());
            leaveRequest.setStartDate(request.getStartDate());
            leaveRequest.setEndDate(request.getEndDate());
            leaveRequest.setReason(request.getReason());
        }

        private LeaveRequest loadLeaveRequest(Long id) {
            return leaveRequestRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));
        }

        private LeaveRequestDisplayDto toDisplay(LeaveRequest leaveRequest) {
            return LeaveRequestDisplayDto.builder()
                    .id(leaveRequest.getId())
                    .employeeId(leaveRequest.getEmployeeId())
                    .leaveType(leaveRequest.getLeaveType())
                    .startDate(leaveRequest.getStartDate())
                    .endDate(leaveRequest.getEndDate())
                    .status(leaveRequest.getStatus())
                    .reason(leaveRequest.getReason())
                    .createdAt(leaveRequest.getCreatedAt())
                    .updatedAt(leaveRequest.getUpdatedAt())
                    .build();
        }
    }
    """,
)

w(
    "hr/controller/LeaveRequestController.java",
    """
    package com.erp.system.hr.controller;

    import com.erp.system.common.dto.ApiResponse;
    import com.erp.system.hr.dto.display.LeaveRequestDisplayDto;
    import com.erp.system.hr.dto.form.LeaveRequestFormDto;
    import com.erp.system.hr.service.LeaveRequestService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/hr/leave-requests")
    @RequiredArgsConstructor
    public class LeaveRequestController {

        private final LeaveRequestService leaveRequestService;

        @GetMapping
        public ApiResponse<List<LeaveRequestDisplayDto>> getAll() {
            return ApiResponse.success(leaveRequestService.getAll());
        }

        @GetMapping("/{id}")
        public ApiResponse<LeaveRequestDisplayDto> getById(@PathVariable Long id) {
            return ApiResponse.success(leaveRequestService.getById(id));
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<LeaveRequestDisplayDto> create(@Valid @RequestBody LeaveRequestFormDto request) {
            return ApiResponse.success(leaveRequestService.create(request));
        }

        @PutMapping("/{id}")
        public ApiResponse<LeaveRequestDisplayDto> update(@PathVariable Long id, @Valid @RequestBody LeaveRequestFormDto request) {
            return ApiResponse.success(leaveRequestService.update(id, request));
        }

        @PostMapping("/{id}/approve")
        public ApiResponse<LeaveRequestDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
            return ApiResponse.success(leaveRequestService.approve(id, actor));
        }

        @PostMapping("/{id}/cancel")
        public ApiResponse<LeaveRequestDisplayDto> cancel(@PathVariable Long id,
                                                          @RequestParam String actor,
                                                          @RequestParam(required = false) String reason) {
            return ApiResponse.success(leaveRequestService.cancel(id, actor, reason));
        }

        @DeleteMapping("/{id}")
        public ApiResponse<Void> delete(@PathVariable Long id) {
            leaveRequestService.delete(id);
            return ApiResponse.success(null);
        }
    }
    """,
)

w(
    "hr/domain/PayrollRun.java",
    """
    package com.erp.system.hr.domain;

    import com.erp.system.common.entity.BaseEntity;
    import com.erp.system.common.enums.TransactionStatus;
    import jakarta.persistence.*;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.LocalDate;

    @Entity
    @Table(name = "payroll_runs", schema = "erp_system")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PayrollRun extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "payroll_number", nullable = false, length = 50, unique = true)
        private String payrollNumber;

        @Column(name = "period_start", nullable = false)
        private LocalDate periodStart;

        @Column(name = "period_end", nullable = false)
        private LocalDate periodEnd;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private TransactionStatus status = TransactionStatus.DRAFT;

        @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal totalAmount = BigDecimal.ZERO;

        @Column(name = "notes", length = 500)
        private String notes;
    }
    """,
)

w(
    "hr/repository/PayrollRunRepository.java",
    """
    package com.erp.system.hr.repository;

    import com.erp.system.hr.domain.PayrollRun;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;

    public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {
        List<PayrollRun> findAllByOrderByIdDesc();
        boolean existsByPayrollNumberIgnoreCase(String payrollNumber);
    }
    """,
)

w(
    "hr/dto/form/PayrollRunFormDto.java",
    """
    package com.erp.system.hr.dto.form;

    import jakarta.validation.constraints.DecimalMin;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.LocalDate;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PayrollRunFormDto {
        @Size(max = 50)
        private String payrollNumber;

        @NotNull
        private LocalDate periodStart;

        @NotNull
        private LocalDate periodEnd;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal totalAmount;

        @Size(max = 500)
        private String notes;
    }
    """,
)

w(
    "hr/dto/display/PayrollRunDisplayDto.java",
    """
    package com.erp.system.hr.dto.display;

    import com.erp.system.common.enums.TransactionStatus;
    import lombok.*;

    import java.math.BigDecimal;
    import java.time.Instant;
    import java.time.LocalDate;
    import java.util.List;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PayrollRunDisplayDto {
        private Long id;
        private String payrollNumber;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private TransactionStatus status;
        private BigDecimal totalAmount;
        private String notes;
        private List<PayrollLineDisplayDto> lines;
        private Instant createdAt;
        private Instant updatedAt;
    }
    """,
)

w(
    "hr/service/PayrollRunService.java",
    """
    package com.erp.system.hr.service;

    import com.erp.system.common.enums.TransactionStatus;
    import com.erp.system.common.exception.BusinessException;
    import com.erp.system.common.exception.ResourceNotFoundException;
    import com.erp.system.common.service.NumberingService;
    import com.erp.system.erp.service.ActivityLogService;
    import com.erp.system.hr.domain.PayrollRun;
    import com.erp.system.hr.dto.display.PayrollLineDisplayDto;
    import com.erp.system.hr.dto.display.PayrollRunDisplayDto;
    import com.erp.system.hr.dto.form.PayrollRunFormDto;
    import com.erp.system.hr.repository.PayrollLineRepository;
    import com.erp.system.hr.repository.PayrollRunRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class PayrollRunService {

        private static final String MODULE = "HR";

        private final PayrollRunRepository payrollRunRepository;
        private final PayrollLineRepository payrollLineRepository;
        private final NumberingService numberingService;
        private final ActivityLogService activityLogService;

        @Transactional(readOnly = true)
        public List<PayrollRunDisplayDto> getAll() {
            return payrollRunRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
        }

        @Transactional(readOnly = true)
        public PayrollRunDisplayDto getById(Long id) {
            return toDisplay(loadPayrollRun(id));
        }

        @Transactional
        public PayrollRunDisplayDto create(PayrollRunFormDto request) {
            PayrollRun payrollRun = new PayrollRun();
            applyForm(payrollRun, request);
            payrollRun.setPayrollNumber(resolveNumber(request.getPayrollNumber()));
            payrollRun.setStatus(TransactionStatus.DRAFT);
            payrollRun = payrollRunRepository.save(payrollRun);
            activityLogService.log(MODULE, "CREATE", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                    "Created payroll run " + payrollRun.getPayrollNumber());
            return toDisplay(payrollRun);
        }

        @Transactional
        public PayrollRunDisplayDto update(Long id, PayrollRunFormDto request) {
            PayrollRun payrollRun = loadPayrollRun(id);
            if (payrollRun.getStatus() == TransactionStatus.APPROVED) {
                throw new BusinessException("Approved payroll run cannot be edited");
            }
            applyForm(payrollRun, request);
            payrollRun = payrollRunRepository.save(payrollRun);
            activityLogService.log(MODULE, "UPDATE", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                    "Updated payroll run " + payrollRun.getPayrollNumber());
            return toDisplay(payrollRun);
        }

        @Transactional
        public PayrollRunDisplayDto approve(Long id, String actor) {
            PayrollRun payrollRun = loadPayrollRun(id);
            if (payrollRun.getStatus() == TransactionStatus.CANCELLED) {
                throw new BusinessException("Cancelled payroll run cannot be approved");
            }
            payrollRun.setStatus(TransactionStatus.APPROVED);
            payrollRun = payrollRunRepository.save(payrollRun);
            activityLogService.log(MODULE, "APPROVE", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                    "Approved payroll run " + payrollRun.getPayrollNumber());
            return toDisplay(payrollRun);
        }

        @Transactional
        public PayrollRunDisplayDto cancel(Long id, String actor, String reason) {
            PayrollRun payrollRun = loadPayrollRun(id);
            payrollRun.setStatus(TransactionStatus.CANCELLED);
            payrollRun = payrollRunRepository.save(payrollRun);
            activityLogService.log(MODULE, "CANCEL", "PayrollRun", payrollRun.getId(), payrollRun.getPayrollNumber(),
                    "Cancelled payroll run " + payrollRun.getPayrollNumber());
            return toDisplay(payrollRun);
        }

        @Transactional
        public void delete(Long id) {
            PayrollRun payrollRun = loadPayrollRun(id);
            if (payrollRun.getStatus() == TransactionStatus.APPROVED) {
                throw new BusinessException("Approved payroll run cannot be deleted");
            }
            payrollRunRepository.delete(payrollRun);
            activityLogService.log(MODULE, "DELETE", "PayrollRun", id, payrollRun.getPayrollNumber(),
                    "Deleted payroll run " + payrollRun.getPayrollNumber());
        }

        private String resolveNumber(String requested) {
            String normalized = requested == null ? null : requested.trim();
            if (normalized != null && !normalized.isEmpty()) {
                if (payrollRunRepository.existsByPayrollNumberIgnoreCase(normalized)) {
                    throw new BusinessException("Payroll number already exists");
                }
                return normalized;
            }
            try {
                return numberingService.generateNextNumber("PAYROLL_RUN");
            } catch (Exception exception) {
                return "PAY-" + System.currentTimeMillis();
            }
        }

        private void applyForm(PayrollRun payrollRun, PayrollRunFormDto request) {
            if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
                throw new BusinessException("Payroll period end cannot be before start");
            }
            payrollRun.setPeriodStart(request.getPeriodStart());
            payrollRun.setPeriodEnd(request.getPeriodEnd());
            payrollRun.setTotalAmount(request.getTotalAmount());
            payrollRun.setNotes(request.getNotes());
        }

        private PayrollRun loadPayrollRun(Long id) {
            return payrollRunRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("PayrollRun", id));
        }

        private PayrollRunDisplayDto toDisplay(PayrollRun payrollRun) {
            List<PayrollLineDisplayDto> lines = payrollLineRepository.findByPayrollIdOrderByIdAsc(payrollRun.getId()).stream()
                    .map(line -> PayrollLineDisplayDto.builder()
                            .id(line.getId())
                            .payrollId(line.getPayrollId())
                            .employeeId(line.getEmployeeId())
                            .basicSalary(line.getBasicSalary())
                            .allowances(line.getAllowances())
                            .deductions(line.getDeductions())
                            .netSalary(line.getNetSalary())
                            .createdAt(line.getCreatedAt())
                            .updatedAt(line.getUpdatedAt())
                            .build())
                    .toList();
            return PayrollRunDisplayDto.builder()
                    .id(payrollRun.getId())
                    .payrollNumber(payrollRun.getPayrollNumber())
                    .periodStart(payrollRun.getPeriodStart())
                    .periodEnd(payrollRun.getPeriodEnd())
                    .status(payrollRun.getStatus())
                    .totalAmount(payrollRun.getTotalAmount())
                    .notes(payrollRun.getNotes())
                    .lines(lines)
                    .createdAt(payrollRun.getCreatedAt())
                    .updatedAt(payrollRun.getUpdatedAt())
                    .build();
        }
    }
    """,
)

w(
    "hr/controller/PayrollRunController.java",
    """
    package com.erp.system.hr.controller;

    import com.erp.system.common.dto.ApiResponse;
    import com.erp.system.hr.dto.display.PayrollRunDisplayDto;
    import com.erp.system.hr.dto.form.PayrollRunFormDto;
    import com.erp.system.hr.service.PayrollRunService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/hr/payroll")
    @RequiredArgsConstructor
    public class PayrollRunController {

        private final PayrollRunService payrollRunService;

        @GetMapping
        public ApiResponse<List<PayrollRunDisplayDto>> getAll() {
            return ApiResponse.success(payrollRunService.getAll());
        }

        @GetMapping("/{id}")
        public ApiResponse<PayrollRunDisplayDto> getById(@PathVariable Long id) {
            return ApiResponse.success(payrollRunService.getById(id));
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public ApiResponse<PayrollRunDisplayDto> create(@Valid @RequestBody PayrollRunFormDto request) {
            return ApiResponse.success(payrollRunService.create(request));
        }

        @PutMapping("/{id}")
        public ApiResponse<PayrollRunDisplayDto> update(@PathVariable Long id, @Valid @RequestBody PayrollRunFormDto request) {
            return ApiResponse.success(payrollRunService.update(id, request));
        }

        @PostMapping("/{id}/approve")
        public ApiResponse<PayrollRunDisplayDto> approve(@PathVariable Long id, @RequestParam String actor) {
            return ApiResponse.success(payrollRunService.approve(id, actor));
        }

        @PostMapping("/{id}/cancel")
        public ApiResponse<PayrollRunDisplayDto> cancel(@PathVariable Long id,
                                                        @RequestParam String actor,
                                                        @RequestParam(required = false) String reason) {
            return ApiResponse.success(payrollRunService.cancel(id, actor, reason));
        }

        @DeleteMapping("/{id}")
        public ApiResponse<Void> delete(@PathVariable Long id) {
            payrollRunService.delete(id);
            return ApiResponse.success(null);
        }
    }
    """,
)

# -----------------------------------------------------------------------------
# CRM
# -----------------------------------------------------------------------------
add_standard_entity(
    module="crm",
    module_code="CRM",
    entity="CrmLead",
    table="crm_leads",
    route="leads",
    domain_fields="""
        @Column(name = "lead_number", nullable = false, length = 50, unique = true)
        private String leadNumber;

        @Column(name = "name", nullable = false, length = 200)
        private String name;

        @Column(name = "company", length = 200)
        private String company;

        @Column(name = "email", length = 190)
        private String email;

        @Column(name = "phone", length = 30)
        private String phone;

        @Column(name = "source", length = 50)
        private String source;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private LeadStatus status = LeadStatus.NEW;

        @Column(name = "customer_id")
        private Long customerId;

        @Column(name = "assigned_to", length = 100)
        private String assignedTo;

        @Column(name = "notes", length = 1000)
        private String notes;
    """,
    form_fields="""
        @NotBlank
        @Size(max = 50)
        private String leadNumber;

        @NotBlank
        @Size(max = 200)
        private String name;

        @Size(max = 200)
        private String company;

        @Email
        @Size(max = 190)
        private String email;

        @Size(max = 30)
        private String phone;

        @Size(max = 50)
        private String source;

        @NotNull
        private LeadStatus status;

        private Long customerId;

        @Size(max = 100)
        private String assignedTo;

        @Size(max = 1000)
        private String notes;
    """,
    display_fields="""
        private String leadNumber;
        private String name;
        private String company;
        private String email;
        private String phone;
        private String source;
        private LeadStatus status;
        private Long customerId;
        private String assignedTo;
        private String notes;
    """,
    apply_form_lines="""
                crmLead.setLeadNumber(request.getLeadNumber().trim());
                crmLead.setName(request.getName().trim());
                crmLead.setCompany(request.getCompany());
                crmLead.setEmail(request.getEmail());
                crmLead.setPhone(request.getPhone());
                crmLead.setSource(request.getSource());
                crmLead.setStatus(request.getStatus());
                crmLead.setCustomerId(request.getCustomerId());
                crmLead.setAssignedTo(request.getAssignedTo());
                crmLead.setNotes(request.getNotes());
    """,
    to_display_lines="""
                        .leadNumber(crmLead.getLeadNumber())
                        .name(crmLead.getName())
                        .company(crmLead.getCompany())
                        .email(crmLead.getEmail())
                        .phone(crmLead.getPhone())
                        .source(crmLead.getSource())
                        .status(crmLead.getStatus())
                        .customerId(crmLead.getCustomerId())
                        .assignedTo(crmLead.getAssignedTo())
                        .notes(crmLead.getNotes())
    """,
    service_imports="import com.erp.system.crm.domain.LeadStatus;",
)

add_standard_entity(
    module="crm",
    module_code="CRM",
    entity="CrmActivity",
    table="crm_activities",
    route="activities",
    domain_fields="""
        @Column(name = "activity_type", nullable = false, length = 30)
        private String activityType;

        @Column(name = "subject", nullable = false, length = 300)
        private String subject;

        @Column(name = "customer_id")
        private Long customerId;

        @Column(name = "lead_id")
        private Long leadId;

        @Column(name = "activity_date", nullable = false)
        private LocalDateTime activityDate;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private CrmActivityStatus status = CrmActivityStatus.PLANNED;

        @Column(name = "notes", length = 1000)
        private String notes;
    """,
    form_fields="""
        @NotBlank
        @Size(max = 30)
        private String activityType;

        @NotBlank
        @Size(max = 300)
        private String subject;

        private Long customerId;
        private Long leadId;

        @NotNull
        private LocalDateTime activityDate;

        @NotNull
        private CrmActivityStatus status;

        @Size(max = 1000)
        private String notes;
    """,
    display_fields="""
        private String activityType;
        private String subject;
        private Long customerId;
        private Long leadId;
        private LocalDateTime activityDate;
        private CrmActivityStatus status;
        private String notes;
    """,
    apply_form_lines="""
                crmActivity.setActivityType(request.getActivityType().trim());
                crmActivity.setSubject(request.getSubject().trim());
                crmActivity.setCustomerId(request.getCustomerId());
                crmActivity.setLeadId(request.getLeadId());
                crmActivity.setActivityDate(request.getActivityDate());
                crmActivity.setStatus(request.getStatus());
                crmActivity.setNotes(request.getNotes());
    """,
    to_display_lines="""
                        .activityType(crmActivity.getActivityType())
                        .subject(crmActivity.getSubject())
                        .customerId(crmActivity.getCustomerId())
                        .leadId(crmActivity.getLeadId())
                        .activityDate(crmActivity.getActivityDate())
                        .status(crmActivity.getStatus())
                        .notes(crmActivity.getNotes())
    """,
    service_imports="import com.erp.system.crm.domain.CrmActivityStatus;",
)

add_standard_entity(
    module="crm",
    module_code="CRM",
    entity="CrmNote",
    table="crm_notes",
    route="notes",
    domain_fields="""
        @Column(name = "customer_id")
        private Long customerId;

        @Column(name = "lead_id")
        private Long leadId;

        @Column(name = "note_text", nullable = false, length = 2000)
        private String noteText;
    """,
    form_fields="""
        private Long customerId;
        private Long leadId;

        @NotBlank
        @Size(max = 2000)
        private String noteText;
    """,
    display_fields="""
        private Long customerId;
        private Long leadId;
        private String noteText;
    """,
    apply_form_lines="""
                crmNote.setCustomerId(request.getCustomerId());
                crmNote.setLeadId(request.getLeadId());
                crmNote.setNoteText(request.getNoteText().trim());
    """,
    to_display_lines="""
                        .customerId(crmNote.getCustomerId())
                        .leadId(crmNote.getLeadId())
                        .noteText(crmNote.getNoteText())
    """,
)

# -----------------------------------------------------------------------------
# Projects
# -----------------------------------------------------------------------------
add_standard_entity(
    module="projects",
    module_code="PROJECTS",
    entity="Project",
    table="projects",
    route="",
    domain_fields="""
        @Column(name = "project_code", nullable = false, length = 50, unique = true)
        private String projectCode;

        @Column(name = "name_en", nullable = false, length = 200)
        private String nameEn;

        @Column(name = "name_ar", length = 200)
        private String nameAr;

        @Column(name = "customer_id")
        private Long customerId;

        @Column(name = "start_date")
        private LocalDate startDate;

        @Column(name = "end_date")
        private LocalDate endDate;

        @Column(name = "budget", nullable = false, precision = 19, scale = 2)
        @Builder.Default
        private BigDecimal budget = BigDecimal.ZERO;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private ProjectStatus status = ProjectStatus.PLANNING;

        @Column(name = "description", length = 1000)
        private String description;
    """,
    form_fields="""
        @NotBlank
        @Size(max = 50)
        private String projectCode;

        @NotBlank
        @Size(max = 200)
        private String nameEn;

        @Size(max = 200)
        private String nameAr;

        private Long customerId;
        private LocalDate startDate;
        private LocalDate endDate;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal budget;

        @NotNull
        private ProjectStatus status;

        @Size(max = 1000)
        private String description;
    """,
    display_fields="""
        private String projectCode;
        private String nameEn;
        private String nameAr;
        private Long customerId;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal budget;
        private ProjectStatus status;
        private String description;
    """,
    apply_form_lines="""
                project.setProjectCode(request.getProjectCode().trim());
                project.setNameEn(request.getNameEn().trim());
                project.setNameAr(request.getNameAr());
                project.setCustomerId(request.getCustomerId());
                project.setStartDate(request.getStartDate());
                project.setEndDate(request.getEndDate());
                project.setBudget(request.getBudget());
                project.setStatus(request.getStatus());
                project.setDescription(request.getDescription());
    """,
    to_display_lines="""
                        .projectCode(project.getProjectCode())
                        .nameEn(project.getNameEn())
                        .nameAr(project.getNameAr())
                        .customerId(project.getCustomerId())
                        .startDate(project.getStartDate())
                        .endDate(project.getEndDate())
                        .budget(project.getBudget())
                        .status(project.getStatus())
                        .description(project.getDescription())
    """,
    service_imports="import java.math.BigDecimal;\nimport com.erp.system.projects.domain.ProjectStatus;",
)

w(
    "projects/controller/ProjectController.java",
    files["projects/controller/ProjectController.java"].replace('@RequestMapping("/projects/")', '@RequestMapping("/projects")'),
)

add_standard_entity(
    module="projects",
    module_code="PROJECTS",
    entity="ProjectTask",
    table="project_tasks",
    route="tasks",
    domain_fields="""
        @Column(name = "project_id", nullable = false)
        private Long projectId;

        @Column(name = "title", nullable = false, length = 300)
        private String title;

        @Column(name = "description", length = 1000)
        private String description;

        @Column(name = "assigned_employee_id")
        private Long assignedEmployeeId;

        @Column(name = "due_date")
        private LocalDate dueDate;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private TaskStatus status = TaskStatus.TODO;

        @Enumerated(EnumType.STRING)
        @Column(name = "priority", nullable = false, length = 10)
        @Builder.Default
        private TaskPriority priority = TaskPriority.MEDIUM;
    """,
    form_fields="""
        @NotNull
        private Long projectId;

        @NotBlank
        @Size(max = 300)
        private String title;

        @Size(max = 1000)
        private String description;

        private Long assignedEmployeeId;
        private LocalDate dueDate;

        @NotNull
        private TaskStatus status;

        @NotNull
        private TaskPriority priority;
    """,
    display_fields="""
        private Long projectId;
        private String title;
        private String description;
        private Long assignedEmployeeId;
        private LocalDate dueDate;
        private TaskStatus status;
        private TaskPriority priority;
    """,
    apply_form_lines="""
                projectTask.setProjectId(request.getProjectId());
                projectTask.setTitle(request.getTitle().trim());
                projectTask.setDescription(request.getDescription());
                projectTask.setAssignedEmployeeId(request.getAssignedEmployeeId());
                projectTask.setDueDate(request.getDueDate());
                projectTask.setStatus(request.getStatus());
                projectTask.setPriority(request.getPriority());
    """,
    to_display_lines="""
                        .projectId(projectTask.getProjectId())
                        .title(projectTask.getTitle())
                        .description(projectTask.getDescription())
                        .assignedEmployeeId(projectTask.getAssignedEmployeeId())
                        .dueDate(projectTask.getDueDate())
                        .status(projectTask.getStatus())
                        .priority(projectTask.getPriority())
    """,
    service_imports="import com.erp.system.projects.domain.TaskPriority;\nimport com.erp.system.projects.domain.TaskStatus;",
)

add_standard_entity(
    module="projects",
    module_code="PROJECTS",
    entity="ProjectExpense",
    table="project_expenses",
    route="expenses",
    domain_fields="""
        @Column(name = "project_id", nullable = false)
        private Long projectId;

        @Column(name = "expense_date", nullable = false)
        private LocalDate expenseDate;

        @Column(name = "description", nullable = false, length = 500)
        private String description;

        @Column(name = "amount", nullable = false, precision = 19, scale = 2)
        private BigDecimal amount;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private com.erp.system.common.enums.TransactionStatus status = com.erp.system.common.enums.TransactionStatus.DRAFT;
    """,
    form_fields="""
        @NotNull
        private Long projectId;

        @NotNull
        private LocalDate expenseDate;

        @NotBlank
        @Size(max = 500)
        private String description;

        @NotNull
        @DecimalMin("0.01")
        private BigDecimal amount;
    """,
    display_fields="""
        private Long projectId;
        private LocalDate expenseDate;
        private String description;
        private BigDecimal amount;
        private TransactionStatus status;
    """,
    apply_form_lines="""
                projectExpense.setProjectId(request.getProjectId());
                projectExpense.setExpenseDate(request.getExpenseDate());
                projectExpense.setDescription(request.getDescription().trim());
                projectExpense.setAmount(request.getAmount());
    """,
    to_display_lines="""
                        .projectId(projectExpense.getProjectId())
                        .expenseDate(projectExpense.getExpenseDate())
                        .description(projectExpense.getDescription())
                        .amount(projectExpense.getAmount())
                        .status(projectExpense.getStatus())
    """,
    service_imports="import java.math.BigDecimal;",
)

add_standard_entity(
    module="projects",
    module_code="PROJECTS",
    entity="ProjectMember",
    table="project_members",
    route="members",
    domain_fields="""
        @Column(name = "project_id", nullable = false)
        private Long projectId;

        @Column(name = "employee_id", nullable = false)
        private Long employeeId;

        @Column(name = "role", length = 50)
        private String role;
    """,
    form_fields="""
        @NotNull
        private Long projectId;

        @NotNull
        private Long employeeId;

        @Size(max = 50)
        private String role;
    """,
    display_fields="""
        private Long projectId;
        private Long employeeId;
        private String role;
    """,
    apply_form_lines="""
                projectMember.setProjectId(request.getProjectId());
                projectMember.setEmployeeId(request.getEmployeeId());
                projectMember.setRole(request.getRole());
    """,
    to_display_lines="""
                        .projectId(projectMember.getProjectId())
                        .employeeId(projectMember.getEmployeeId())
                        .role(projectMember.getRole())
    """,
)

# -----------------------------------------------------------------------------
# Inventory StockService requested by purchases approval flow
# -----------------------------------------------------------------------------
w(
    "inventory/service/StockService.java",
    """
    package com.erp.system.inventory.service;

    import com.erp.system.common.enums.StockMovementType;
    import com.erp.system.common.enums.TransactionStatus;
    import com.erp.system.common.exception.BusinessException;
    import com.erp.system.common.exception.ResourceNotFoundException;
    import com.erp.system.common.service.NumberingService;
    import com.erp.system.inventory.domain.Product;
    import com.erp.system.inventory.domain.StockLevel;
    import com.erp.system.inventory.domain.StockMovement;
    import com.erp.system.inventory.domain.Warehouse;
    import com.erp.system.inventory.repository.ProductRepository;
    import com.erp.system.inventory.repository.StockLevelRepository;
    import com.erp.system.inventory.repository.StockMovementRepository;
    import com.erp.system.inventory.repository.WarehouseRepository;
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
        public void receiveStock(Long productId,
                                 Long warehouseId,
                                 BigDecimal quantity,
                                 BigDecimal unitCost,
                                 String referenceType,
                                 Long referenceId,
                                 LocalDate movementDate) {
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Quantity must be greater than zero");
            }
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));

            StockLevel level = stockLevelRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                    .orElseGet(() -> StockLevel.builder()
                            .product(product)
                            .warehouse(warehouse)
                            .quantity(BigDecimal.ZERO)
                            .reservedQuantity(BigDecimal.ZERO)
                            .build());
            level.setQuantity(level.getQuantity().add(quantity));
            stockLevelRepository.save(level);

            stockMovementRepository.save(StockMovement.builder()
                    .movementNumber(generateMovementNumber())
                    .movementDate(movementDate == null ? LocalDate.now() : movementDate)
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

        private String generateMovementNumber() {
            try {
                return numberingService.generateNextNumber("STOCK_MOVEMENT");
            } catch (Exception exception) {
                return "SM-" + System.currentTimeMillis();
            }
        }
    }
    """,
)

# -----------------------------------------------------------------------------
# Module counts + required final write loop
# -----------------------------------------------------------------------------
module_counts: dict[str, int] = {}
for rel in files:
    module = rel.split("/", 1)[0]
    module_counts[module] = module_counts.get(module, 0) + 1

print("Module file counts:")
for module_name in ("purchases", "hr", "crm", "projects"):
    print(f"{module_name}: {module_counts.get(module_name, 0)}")

BASE = Path(r"d:\Apps Work\My Apps\erp Project\erp-system-backend\src\main\java\com\erp\system")
for rel, content in files.items():
    path = BASE / rel.replace("/", os.sep)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    print(path)
print(f"Wrote {len(files)} files")
