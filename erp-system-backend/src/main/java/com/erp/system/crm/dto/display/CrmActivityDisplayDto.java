package com.erp.system.crm.dto.display;

import com.erp.system.crm.domain.CrmActivityStatus;
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
public class CrmActivityDisplayDto {
    private Long id;

private String activityType;
private String subject;
private Long customerId;
private Long leadId;
private LocalDateTime activityDate;
private CrmActivityStatus status;
private String notes;

    private Instant createdAt;
    private Instant updatedAt;
}
