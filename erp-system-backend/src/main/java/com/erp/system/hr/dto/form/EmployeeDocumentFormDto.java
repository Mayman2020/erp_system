package com.erp.system.hr.dto.form;

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
public class EmployeeDocumentFormDto {

@NotNull
private Long employeeId;

@NotBlank
@Size(max = 50)
private String documentType;

@NotBlank
@Size(max = 255)
private String fileName;

@Size(max = 500)
private String filePath;

private LocalDate expiryDate;

}
