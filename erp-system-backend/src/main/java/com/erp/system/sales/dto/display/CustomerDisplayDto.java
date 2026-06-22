package com.erp.system.sales.dto.display;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class CustomerDisplayDto {

    private Long id;
    private String code;
    private String nameEn;
    private String nameAr;
    private String email;
    private String phone;
    private String taxNumber;
    private String address;
    private BigDecimal creditLimit;
    private Long receivableAccountId;
    private String receivableAccountCode;
    private String receivableAccountName;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
