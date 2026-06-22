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
@Table(name = "employees", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "employee_code", nullable = false, length = 50, unique = true)
private String employeeCode;

@Column(name = "full_name_en", nullable = false, length = 200)
private String fullNameEn;

@Column(name = "full_name_ar", length = 200)
private String fullNameAr;

@Column(name = "email", length = 190)
private String email;

@Column(name = "phone", length = 30)
private String phone;

@Column(name = "department_id")
private Long departmentId;

@Column(name = "job_title", length = 150)
private String jobTitle;

@Column(name = "hire_date")
private LocalDate hireDate;

@Column(name = "basic_salary", nullable = false, precision = 19, scale = 2)
@Builder.Default
private BigDecimal basicSalary = BigDecimal.ZERO;

@Column(name = "is_active", nullable = false)
@Builder.Default
private boolean active = true;

}
