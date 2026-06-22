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
public class PurchaseOrderLineFormDto {

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

}
