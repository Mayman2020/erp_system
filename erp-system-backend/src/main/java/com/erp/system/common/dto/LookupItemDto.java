package com.erp.system.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LookupItemDto {
    private Long id;
    private String typeCode;
    private String code;
    private String nameEn;
    private String nameAr;
    private Integer sortOrder;
    private Boolean active;
    private String icon;
}
