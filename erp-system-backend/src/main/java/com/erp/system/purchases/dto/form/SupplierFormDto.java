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
public class SupplierFormDto {

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

}
