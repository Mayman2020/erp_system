package com.erp.system.hr.dto.display;

import com.erp.system.common.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDocumentDisplayDto {
    private Long id;

private Long employeeId;
private String documentType;
private String fileName;
private String filePath;
private LocalDate expiryDate;

    private Instant createdAt;
    private Instant updatedAt;
}
