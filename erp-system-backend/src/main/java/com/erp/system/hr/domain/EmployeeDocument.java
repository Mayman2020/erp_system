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
@Table(name = "employee_documents", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "employee_id", nullable = false)
private Long employeeId;

@Column(name = "document_type", nullable = false, length = 50)
private String documentType;

@Column(name = "file_name", nullable = false, length = 255)
private String fileName;

@Column(name = "file_path", length = 500)
private String filePath;

@Column(name = "expiry_date")
private LocalDate expiryDate;

}
