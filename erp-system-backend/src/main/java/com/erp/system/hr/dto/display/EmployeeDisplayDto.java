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
public class EmployeeDisplayDto {
    private Long id;

private String employeeCode;
private String fullNameEn;
private String fullNameAr;
private String email;
private String phone;
private Long departmentId;
private String jobTitle;
private LocalDate hireDate;
private BigDecimal basicSalary;
private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
