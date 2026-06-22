package com.erp.system.projects.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "project_expenses", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectExpense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "project_id", nullable = false)
private Long projectId;

@Column(name = "expense_date", nullable = false)
private LocalDate expenseDate;

@Column(name = "description", nullable = false, length = 500)
private String description;

@Column(name = "amount", nullable = false, precision = 19, scale = 2)
private BigDecimal amount;

@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
@Builder.Default
private com.erp.system.common.enums.TransactionStatus status = com.erp.system.common.enums.TransactionStatus.DRAFT;

}
