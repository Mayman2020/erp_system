package com.erp.system.purchases.domain;

import com.erp.system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "suppliers", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "code", nullable = false, length = 50, unique = true)
private String code;

@Column(name = "name_en", nullable = false, length = 200)
private String nameEn;

@Column(name = "name_ar", length = 200)
private String nameAr;

@Column(name = "email", length = 190)
private String email;

@Column(name = "phone", length = 30)
private String phone;

@Column(name = "tax_number", length = 50)
private String taxNumber;

@Column(name = "address", length = 500)
private String address;

@Column(name = "payable_account_id")
private Long payableAccountId;

@Column(name = "is_active", nullable = false)
@Builder.Default
private boolean active = true;

}
