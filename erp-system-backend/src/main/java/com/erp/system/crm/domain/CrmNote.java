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
@Table(name = "crm_notes", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "customer_id")
private Long customerId;

@Column(name = "lead_id")
private Long leadId;

@Column(name = "note_text", nullable = false, length = 2000)
private String noteText;

}
