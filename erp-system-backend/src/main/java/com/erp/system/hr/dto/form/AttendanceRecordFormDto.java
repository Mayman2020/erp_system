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
public class AttendanceRecordFormDto {

@NotNull
private Long employeeId;

@NotNull
private LocalDate attendanceDate;

private LocalTime checkIn;
private LocalTime checkOut;

@NotBlank
@Size(max = 20)
private String status;

@Size(max = 300)
private String notes;

}
