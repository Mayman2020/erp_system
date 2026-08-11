package com.erp.system.partners.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDisplayDto {
    private Long id;
    private String code;
    private String name;
    private BigDecimal sharePercent;
    private Long capitalAccountId;
    private String capitalAccountCode;
    private String capitalAccountName;
    private Long drawingAccountId;
    private String drawingAccountCode;
    private String drawingAccountName;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
