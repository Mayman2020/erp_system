package com.erp.system.digitalliteracy.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "digital_enrollments", schema = "erp_system")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitalEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "progress_pct", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressPct = BigDecimal.ZERO;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ENROLLED";

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "certificate_no", length = 60)
    private String certificateNo;
}
