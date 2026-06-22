package com.erp.system.crm.dto.form;

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
public class CrmNoteFormDto {

private Long customerId;
private Long leadId;

@NotBlank
@Size(max = 2000)
private String noteText;

}
