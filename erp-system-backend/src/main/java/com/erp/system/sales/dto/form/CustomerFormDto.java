package com.erp.system.sales.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerFormDto {

    private String code;

    @NotBlank(message = "VALIDATION.REQUIRED")
    @Size(max = 200)
    private String nameEn;

    @Size(max = 200)
    private String nameAr;

    @Size(max = 190)
    private String email;

    @Size(max = 30)
    private String phone;

    @Size(max = 50)
    private String taxNumber;

    @Size(max = 500)
    private String address;

    @DecimalMin(value = "0.00", message = "VALIDATION.NON_NEGATIVE")
    private BigDecimal creditLimit;

    private Long receivableAccountId;

    private Boolean active;
}
