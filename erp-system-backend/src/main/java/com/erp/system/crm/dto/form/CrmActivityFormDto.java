package com.erp.system.crm.dto.form;

import com.erp.system.crm.domain.CrmActivityStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmActivityFormDto {

@NotBlank
@Size(max = 30)
private String activityType;

@NotBlank
@Size(max = 300)
private String subject;

private Long customerId;
private Long leadId;

@NotNull
private LocalDateTime activityDate;

@NotNull
private CrmActivityStatus status;

@Size(max = 1000)
private String notes;

}
