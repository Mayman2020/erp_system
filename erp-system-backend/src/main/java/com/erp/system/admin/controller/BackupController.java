package com.erp.system.admin.controller;

import com.erp.system.admin.dto.display.BackupJobDisplayDto;
import com.erp.system.admin.service.BackupService;
import com.erp.system.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/admin/backups")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    @GetMapping
    public ApiResponse<List<BackupJobDisplayDto>> getAll() {
        return ApiResponse.success(backupService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<BackupJobDisplayDto> getById(@PathVariable Long id) {
        return ApiResponse.success(backupService.getById(id));
    }

    @PostMapping
    public ApiResponse<BackupJobDisplayDto> create() {
        return ApiResponse.success(backupService.createManualBackup());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        BackupJobDisplayDto metadata = backupService.getById(id);
        Resource resource = backupService.downloadFile(id);
        String fileName = metadata.getFilePath() == null ? "backup.sql" : Path.of(metadata.getFilePath()).getFileName().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
