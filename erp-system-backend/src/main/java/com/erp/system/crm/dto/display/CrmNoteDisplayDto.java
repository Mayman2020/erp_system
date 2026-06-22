package com.erp.system.crm.dto.display;

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
public class CrmNoteDisplayDto {
    private Long id;

private Long customerId;
private Long leadId;
private String noteText;

    private Instant createdAt;
    private Instant updatedAt;
}
