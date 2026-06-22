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
@Table(name = "departments", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "code", nullable = false, length = 30, unique = true)
private String code;

@Column(name = "name_en", nullable = false, length = 150)
private String nameEn;

@Column(name = "name_ar", length = 150)
private String nameAr;

@Column(name = "manager_id")
private Long managerId;

@Column(name = "is_active", nullable = false)
@Builder.Default
private boolean active = true;

}
