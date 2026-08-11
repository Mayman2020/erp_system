package com.erp.system.admin.service;

import com.erp.system.admin.config.BackupProperties;
import com.erp.system.admin.domain.BackupJob;
import com.erp.system.admin.dto.display.BackupJobDisplayDto;
import com.erp.system.admin.repository.BackupJobRepository;
import com.erp.system.common.exception.BusinessException;
import com.erp.system.common.exception.ResourceNotFoundException;
import com.erp.system.common.security.SecurityUtils;
import com.erp.system.common.service.NumberingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BackupService {

    private final BackupJobRepository backupJobRepository;
    private final BackupProperties backupProperties;
    private final NumberingService numberingService;

    @Transactional(readOnly = true)
    public List<BackupJobDisplayDto> getAll() {
        return backupJobRepository.findAllByOrderByIdDesc().stream().map(this::toDisplay).toList();
    }

    @Transactional(readOnly = true)
    public BackupJobDisplayDto getById(Long id) {
        return toDisplay(load(id));
    }

    @Transactional
    public BackupJobDisplayDto createManualBackup() {
        BackupJob job = new BackupJob();
        job.setJobNo(resolveJobNo());
        job.setStatus("RUNNING");
        job.setTriggerType("MANUAL");
        job.setStartedAt(Instant.now());
        job.setCreatedBy(SecurityUtils.currentUsername());
        job = backupJobRepository.save(job);

        try {
            Path outputDir = Path.of(backupProperties.getDir()).toAbsolutePath().normalize();
            Files.createDirectories(outputDir);
            String fileName = "erp-backup-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(Instant.now().atZone(java.time.ZoneId.systemDefault())) + ".sql";
            Path outputFile = outputDir.resolve(fileName);

            List<String> command = List.of(
                    backupProperties.getPgDumpPath(),
                    "-h", backupProperties.getHost(),
                    "-p", backupProperties.getPort(),
                    "-U", backupProperties.getUser(),
                    "-d", backupProperties.getDb(),
                    "-f", outputFile.toString()
            );

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("PGPASSWORD", backupProperties.getPassword() == null ? "" : backupProperties.getPassword());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BusinessException("pg_dump failed with exit code " + exitCode);
            }

            long size = Files.size(outputFile);
            String checksum = sha256Hex(outputFile);
            job.setStatus("COMPLETED");
            job.setFilePath(outputFile.toString());
            job.setFileSizeBytes(size);
            job.setChecksumSha256(checksum);
            job.setFinishedAt(Instant.now());
        } catch (Exception exception) {
            job.setStatus("FAILED");
            job.setErrorMessage(exception.getMessage());
            job.setFinishedAt(Instant.now());
        }

        job = backupJobRepository.save(job);
        return toDisplay(job);
    }

    @Transactional(readOnly = true)
    public Resource downloadFile(Long id) {
        BackupJob job = load(id);
        if (job.getFilePath() == null || job.getFilePath().isBlank()) {
            throw new BusinessException("Backup file is not available");
        }
        Path path = Path.of(job.getFilePath());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("BackupFile", id);
        }
        return new FileSystemResource(path);
    }

    private BackupJob load(Long id) {
        return backupJobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("BackupJob", id));
    }

    private String resolveJobNo() {
        try {
            return numberingService.generateNextNumber("BACKUP_JOB");
        } catch (Exception exception) {
            return "BKP-" + System.currentTimeMillis();
        }
    }

    private static String sha256Hex(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream inputStream = Files.newInputStream(path);
             DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest)) {
            digestInputStream.transferTo(java.io.OutputStream.nullOutputStream());
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private BackupJobDisplayDto toDisplay(BackupJob job) {
        boolean downloadable = "COMPLETED".equals(job.getStatus())
                && job.getFilePath() != null
                && Files.exists(Path.of(job.getFilePath()));
        return BackupJobDisplayDto.builder()
                .id(job.getId())
                .jobNo(job.getJobNo())
                .status(job.getStatus())
                .triggerType(job.getTriggerType())
                .filePath(job.getFilePath())
                .fileSizeBytes(job.getFileSizeBytes())
                .checksumSha256(job.getChecksumSha256())
                .errorMessage(job.getErrorMessage())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .createdAt(job.getCreatedAt())
                .createdBy(job.getCreatedBy())
                .downloadable(downloadable)
                .build();
    }
}
