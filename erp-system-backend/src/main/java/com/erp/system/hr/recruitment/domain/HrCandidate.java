package com.erp.system.hr.recruitment.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hr_candidates", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrCandidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "vacancy_id")
    private Long vacancyId;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "APPLIED";

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "notes", length = 500)
    private String notes;
}
