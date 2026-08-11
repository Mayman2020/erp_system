package com.erp.system.partners.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitDistributionLineDisplayDto {
    private Long id;
    private Long partnerId;
    private String partnerCode;
    private String partnerName;
    private BigDecimal sharePercent;
    private BigDecimal amount;
}
