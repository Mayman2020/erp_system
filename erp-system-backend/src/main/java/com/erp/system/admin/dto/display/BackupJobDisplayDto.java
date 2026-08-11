package com.erp.system.admin.dto.display;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class BackupJobDisplayDto {
    Long id;
    String jobNo;
    String status;
    String triggerType;
    String filePath;
    Long fileSizeBytes;
    String checksumSha256;
    String errorMessage;
    Instant startedAt;
    Instant finishedAt;
    Instant createdAt;
    String createdBy;
    boolean downloadable;
}
