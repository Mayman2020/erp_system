package com.erp.system.hr.recruitment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "leave_balances", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "leave_type", nullable = false, length = 40)
    private String leaveType;

    @Column(name = "balance_days", nullable = false, precision = 9, scale = 2)
    @Builder.Default
    private BigDecimal balanceDays = BigDecimal.ZERO;

    @Column(name = "year", nullable = false)
    private Integer year;
}
