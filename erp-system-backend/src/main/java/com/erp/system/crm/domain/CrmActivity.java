package com.erp.system.crm.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "crm_activities", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmActivity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "activity_type", nullable = false, length = 30)
private String activityType;

@Column(name = "subject", nullable = false, length = 300)
private String subject;

@Column(name = "customer_id")
private Long customerId;

@Column(name = "lead_id")
private Long leadId;

@Column(name = "activity_date", nullable = false)
private LocalDateTime activityDate;

@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
@Builder.Default
private CrmActivityStatus status = CrmActivityStatus.PLANNED;

@Column(name = "notes", length = 1000)
private String notes;

}
