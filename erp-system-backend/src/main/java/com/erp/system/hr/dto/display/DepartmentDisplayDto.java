package com.erp.system.hr.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDisplayDto {
    private Long id;

private String code;
private String nameEn;
private String nameAr;
private Long managerId;
private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
}
