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
public class PurchaseReturnLineFormDto {

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

}
