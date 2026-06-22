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
@Table(name = "crm_leads", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmLead extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "lead_number", nullable = false, length = 50, unique = true)
private String leadNumber;

@Column(name = "name", nullable = false, length = 200)
private String name;

@Column(name = "company", length = 200)
private String company;

@Column(name = "email", length = 190)
private String email;

@Column(name = "phone", length = 30)
private String phone;

@Column(name = "source", length = 50)
private String source;

@Enumerated(EnumType.STRING)
@Column(name = "status", nullable = false, length = 20)
@Builder.Default
private LeadStatus status = LeadStatus.NEW;

@Column(name = "customer_id")
private Long customerId;

@Column(name = "assigned_to", length = 100)
private String assignedTo;

@Column(name = "notes", length = 1000)
private String notes;

}
