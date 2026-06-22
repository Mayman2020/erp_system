package com.erp.system.purchases.dto.form;

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
public class PurchaseOrderFormDto {

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

}
