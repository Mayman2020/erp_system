package com.erp.system.inventory.dto.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasureDisplayDto {

    private Long id;
    private String code;
    private String name;
    private String nameEn;
    private String nameAr;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
