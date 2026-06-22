package com.erp.system.crm.dto.display;

import com.erp.system.crm.domain.LeadStatus;
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
public class CrmLeadDisplayDto {
    private Long id;

private String leadNumber;
private String name;
private String company;
private String email;
private String phone;
private String source;
private LeadStatus status;
private Long customerId;
private String assignedTo;
private String notes;

    private Instant createdAt;
    private Instant updatedAt;
}
