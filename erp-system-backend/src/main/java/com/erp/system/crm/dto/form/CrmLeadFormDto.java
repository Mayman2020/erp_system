package com.erp.system.crm.dto.form;

import com.erp.system.crm.domain.LeadStatus;
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
public class CrmLeadFormDto {

@NotBlank
@Size(max = 50)
private String leadNumber;

@NotBlank
@Size(max = 200)
private String name;

@Size(max = 200)
private String company;

@Email
@Size(max = 190)
private String email;

@Size(max = 30)
private String phone;

@Size(max = 50)
private String source;

@NotNull
private LeadStatus status;

private Long customerId;

@Size(max = 100)
private String assignedTo;

@Size(max = 1000)
private String notes;

}
