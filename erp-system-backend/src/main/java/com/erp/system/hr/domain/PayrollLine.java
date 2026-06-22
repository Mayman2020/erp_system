package com.erp.system.hr.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "payroll_lines", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "payroll_id", nullable = false)
private Long payrollId;

@Column(name = "employee_id", nullable = false)
private Long employeeId;

@Column(name = "basic_salary", nullable = false, precision = 19, scale = 2)
@Builder.Default
private BigDecimal basicSalary = BigDecimal.ZERO;

@Column(name = "allowances", nullable = false, precision = 19, scale = 2)
@Builder.Default
private BigDecimal allowances = BigDecimal.ZERO;

@Column(name = "deductions", nullable = false, precision = 19, scale = 2)
@Builder.Default
private BigDecimal deductions = BigDecimal.ZERO;

@Column(name = "net_salary", nullable = false, precision = 19, scale = 2)
@Builder.Default
private BigDecimal netSalary = BigDecimal.ZERO;

}
