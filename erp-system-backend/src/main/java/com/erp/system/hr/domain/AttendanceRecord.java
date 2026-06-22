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
@Table(name = "attendance_records", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


@Column(name = "employee_id", nullable = false)
private Long employeeId;

@Column(name = "attendance_date", nullable = false)
private LocalDate attendanceDate;

@Column(name = "check_in")
private LocalTime checkIn;

@Column(name = "check_out")
private LocalTime checkOut;

@Column(name = "status", nullable = false, length = 20)
private String status;

@Column(name = "notes", length = 300)
private String notes;

}
